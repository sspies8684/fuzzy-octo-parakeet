use anyhow::{Context, Result};
use std::collections::{HashMap, HashSet};
use std::net::{IpAddr, SocketAddr};
use std::sync::Arc;
use tokio::net::{TcpStream, UdpSocket};
use tokio::sync::Semaphore;
use tokio::time::timeout;
use tracing::{debug, trace, warn};

use crate::config::{HostConfig, ScannerConfig};

/// Result of scanning a single port
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum PortStatus {
    /// Port is open (accepting connections)
    Open,
    /// Port is closed (connection refused or reset)
    Closed,
    /// Port status is filtered/unknown (timeout, no response)
    Filtered,
}

/// Result of scanning all configured ports on a host
#[derive(Debug, Clone)]
pub struct HostScanResult {
    /// The host that was scanned
    pub host_name: String,
    /// The IP address that was scanned
    pub ip_address: IpAddr,
    /// TCP port scan results: port -> status
    pub tcp_results: HashMap<u16, PortStatus>,
    /// UDP port scan results: port -> status
    pub udp_results: HashMap<u16, PortStatus>,
    /// TCP ports that are open but shouldn't be
    pub tcp_unexpected_open: HashSet<u16>,
    /// TCP ports that are closed but should be open
    pub tcp_unexpected_closed: HashSet<u16>,
    /// UDP ports that are open but shouldn't be
    pub udp_unexpected_open: HashSet<u16>,
    /// UDP ports that are closed but should be open
    pub udp_unexpected_closed: HashSet<u16>,
}

/// Port scanner with configurable parallelism and timeouts
pub struct Scanner {
    config: ScannerConfig,
    semaphore: Arc<Semaphore>,
}

#[allow(dead_code)]
impl Scanner {
    /// Create a new scanner with the given configuration
    pub async fn new(config: ScannerConfig) -> Result<Self> {
        let semaphore = Arc::new(Semaphore::new(config.parallelism));

        Ok(Self { config, semaphore })
    }

    /// Resolve a hostname to an IP address using system DNS
    pub async fn resolve_host(&self, hostname: &str) -> Result<IpAddr> {
        // First try to parse as IP address directly
        if let Ok(ip) = hostname.parse::<IpAddr>() {
            return Ok(ip);
        }

        // Resolve via system DNS (runs in blocking thread pool)
        let hostname_owned = hostname.to_string();
        let hostname_for_error = hostname_owned.clone();
        let dns_timeout = self.config.dns_timeout();
        
        let result = timeout(dns_timeout, tokio::task::spawn_blocking(move || {
            // Use dns-lookup for resolution
            dns_lookup::lookup_host(&hostname_owned)
        }))
        .await
        .with_context(|| format!("DNS resolution timeout for {}", hostname_for_error))?
        .with_context(|| "DNS resolution task failed")?
        .with_context(|| "Failed to resolve hostname")?;

        result
            .into_iter()
            .next()
            .ok_or_else(|| anyhow::anyhow!("No IP addresses found for hostname"))
    }

    /// Scan a TCP port
    pub async fn scan_tcp_port(&self, addr: SocketAddr) -> PortStatus {
        let _permit = self.semaphore.acquire().await.unwrap();

        trace!("Scanning TCP {}:{}", addr.ip(), addr.port());

        match timeout(self.config.tcp_timeout(), TcpStream::connect(addr)).await {
            Ok(Ok(_stream)) => {
                debug!("TCP port {} is open on {}", addr.port(), addr.ip());
                PortStatus::Open
            }
            Ok(Err(e)) => {
                // Connection refused or reset means the port is closed
                let error_str = e.to_string().to_lowercase();
                if error_str.contains("refused") || error_str.contains("reset") {
                    trace!("TCP port {} is closed on {}", addr.port(), addr.ip());
                    PortStatus::Closed
                } else {
                    trace!(
                        "TCP port {} is filtered on {} (error: {})",
                        addr.port(),
                        addr.ip(),
                        e
                    );
                    PortStatus::Filtered
                }
            }
            Err(_) => {
                // Timeout - port is filtered
                trace!("TCP port {} timed out on {}", addr.port(), addr.ip());
                PortStatus::Filtered
            }
        }
    }

    /// Scan a UDP port
    ///
    /// Note: UDP scanning is inherently unreliable. We send a probe packet and wait
    /// for a response. No response typically means the port is open or filtered,
    /// while an ICMP port unreachable message means it's closed.
    pub async fn scan_udp_port(&self, addr: SocketAddr) -> PortStatus {
        let _permit = self.semaphore.acquire().await.unwrap();

        trace!("Scanning UDP {}:{}", addr.ip(), addr.port());

        // Bind to a random local port
        let bind_addr: SocketAddr = if addr.is_ipv4() {
            "0.0.0.0:0".parse().unwrap()
        } else {
            "[::]:0".parse().unwrap()
        };

        let socket = match UdpSocket::bind(bind_addr).await {
            Ok(s) => s,
            Err(e) => {
                warn!("Failed to bind UDP socket: {}", e);
                return PortStatus::Filtered;
            }
        };

        if let Err(e) = socket.connect(addr).await {
            warn!("Failed to connect UDP socket to {}: {}", addr, e);
            return PortStatus::Filtered;
        }

        // Send a probe packet
        // For common UDP services, we could send service-specific probes
        let probe = self.get_udp_probe(addr.port());

        if let Err(e) = socket.send(&probe).await {
            warn!("Failed to send UDP probe to {}: {}", addr, e);
            return PortStatus::Filtered;
        }

        // Wait for a response
        let mut buf = [0u8; 1024];
        match timeout(self.config.udp_timeout(), socket.recv(&mut buf)).await {
            Ok(Ok(_)) => {
                // Got a response - port is open
                debug!("UDP port {} is open on {}", addr.port(), addr.ip());
                PortStatus::Open
            }
            Ok(Err(e)) => {
                // Error receiving - check if it's an ICMP port unreachable
                let error_str = e.to_string().to_lowercase();
                if error_str.contains("refused")
                    || error_str.contains("unreachable")
                    || error_str.contains("reset")
                {
                    trace!("UDP port {} is closed on {}", addr.port(), addr.ip());
                    PortStatus::Closed
                } else {
                    trace!(
                        "UDP port {} is filtered on {} (error: {})",
                        addr.port(),
                        addr.ip(),
                        e
                    );
                    PortStatus::Filtered
                }
            }
            Err(_) => {
                // Timeout - port is likely open or filtered
                // For UDP, no response often means open (service didn't respond)
                // We'll mark it as filtered since we can't be sure
                trace!("UDP port {} timed out on {}", addr.port(), addr.ip());
                PortStatus::Filtered
            }
        }
    }

    /// Get a UDP probe packet for common services
    fn get_udp_probe(&self, port: u16) -> Vec<u8> {
        match port {
            // DNS - simple query
            53 => vec![
                0x00, 0x01, // Transaction ID
                0x01, 0x00, // Flags: Standard query
                0x00, 0x01, // Questions: 1
                0x00, 0x00, // Answer RRs
                0x00, 0x00, // Authority RRs
                0x00, 0x00, // Additional RRs
                0x07, b'v', b'e', b'r', b's', b'i', b'o', b'n', // "version"
                0x04, b'b', b'i', b'n', b'd', // ".bind"
                0x00, // Root
                0x00, 0x10, // Type: TXT
                0x00, 0x03, // Class: CHAOS
            ],
            // NTP
            123 => vec![
                0xe3, 0x00, 0x04, 0xfa, // Flags, stratum, poll, precision
                0x00, 0x01, 0x00, 0x00, // Root delay
                0x00, 0x01, 0x00, 0x00, // Root dispersion
                0x00, 0x00, 0x00, 0x00, // Reference ID
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Reference timestamp
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Origin timestamp
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Receive timestamp
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Transmit timestamp
            ],
            // SNMP - GetRequest for sysDescr
            161 => vec![
                0x30, 0x26, // SEQUENCE, length
                0x02, 0x01, 0x00, // INTEGER: version (0 = SNMPv1)
                0x04, 0x06, b'p', b'u', b'b', b'l', b'i', b'c', // OCTET STRING: community "public"
                0xa0, 0x19, // GetRequest PDU
                0x02, 0x01, 0x00, // Request ID
                0x02, 0x01, 0x00, // Error status
                0x02, 0x01, 0x00, // Error index
                0x30, 0x0e, // Varbind list
                0x30, 0x0c, // Varbind
                0x06, 0x08, 0x2b, 0x06, 0x01, 0x02, 0x01, 0x01, 0x01, 0x00, // OID: sysDescr
                0x05, 0x00, // NULL
            ],
            // Default: send empty packet
            _ => vec![0x00],
        }
    }

    /// Scan all configured ports on a host
    pub async fn scan_host(&self, host: &HostConfig) -> Result<HostScanResult> {
        // Resolve IP address
        let ip_address = if let Some(ip) = host.ip {
            ip
        } else {
            self.resolve_host(&host.name).await?
        };

        debug!("Scanning host {} ({})", host.name, ip_address);

        // Get ports to scan
        let tcp_ports: Vec<u16> = host.tcp_ports_to_scan().into_iter().collect();
        let udp_ports: Vec<u16> = host.udp_ports_to_scan().into_iter().collect();

        // Get expected open ports
        let expected_tcp = host.expected_tcp_open();
        let expected_udp = host.expected_udp_open();

        // Scan TCP ports
        let mut tcp_results = HashMap::new();
        for chunk in tcp_ports.chunks(self.config.parallelism) {
            let mut handles = Vec::new();
            for &port in chunk {
                let addr = SocketAddr::new(ip_address, port);
                let scanner = self.clone_scanner_ref();
                handles.push(tokio::spawn(async move {
                    let status = scanner.scan_tcp_port(addr).await;
                    (port, status)
                }));
            }

            for handle in handles {
                if let Ok((port, status)) = handle.await {
                    tcp_results.insert(port, status);
                }
            }

            // Apply delay between batches
            if self.config.scan_delay_ms > 0 {
                tokio::time::sleep(self.config.scan_delay()).await;
            }
        }

        // Scan UDP ports
        let mut udp_results = HashMap::new();
        for chunk in udp_ports.chunks(self.config.parallelism) {
            let mut handles = Vec::new();
            for &port in chunk {
                let addr = SocketAddr::new(ip_address, port);
                let scanner = self.clone_scanner_ref();
                handles.push(tokio::spawn(async move {
                    let status = scanner.scan_udp_port(addr).await;
                    (port, status)
                }));
            }

            for handle in handles {
                if let Ok((port, status)) = handle.await {
                    udp_results.insert(port, status);
                }
            }

            // Apply delay between batches
            if self.config.scan_delay_ms > 0 {
                tokio::time::sleep(self.config.scan_delay()).await;
            }
        }

        // Calculate unexpected states
        let tcp_unexpected_open: HashSet<u16> = tcp_results
            .iter()
            .filter(|(port, status)| **status == PortStatus::Open && !expected_tcp.contains(port))
            .map(|(port, _)| *port)
            .collect();

        let tcp_unexpected_closed: HashSet<u16> = expected_tcp
            .iter()
            .filter(|port| {
                tcp_results
                    .get(port)
                    .map(|s| *s != PortStatus::Open)
                    .unwrap_or(true)
            })
            .copied()
            .collect();

        let udp_unexpected_open: HashSet<u16> = udp_results
            .iter()
            .filter(|(port, status)| **status == PortStatus::Open && !expected_udp.contains(port))
            .map(|(port, _)| *port)
            .collect();

        let udp_unexpected_closed: HashSet<u16> = expected_udp
            .iter()
            .filter(|port| {
                udp_results
                    .get(port)
                    .map(|s| *s != PortStatus::Open)
                    .unwrap_or(true)
            })
            .copied()
            .collect();

        Ok(HostScanResult {
            host_name: host.name.clone(),
            ip_address,
            tcp_results,
            udp_results,
            tcp_unexpected_open,
            tcp_unexpected_closed,
            udp_unexpected_open,
            udp_unexpected_closed,
        })
    }

    /// Clone scanner reference for spawning tasks
    fn clone_scanner_ref(&self) -> ScannerRef {
        ScannerRef {
            config: self.config.clone(),
            semaphore: Arc::clone(&self.semaphore),
        }
    }
}

/// Lightweight reference to scanner for spawning tasks
struct ScannerRef {
    config: ScannerConfig,
    semaphore: Arc<Semaphore>,
}

impl ScannerRef {
    async fn scan_tcp_port(&self, addr: SocketAddr) -> PortStatus {
        let _permit = self.semaphore.acquire().await.unwrap();

        trace!("Scanning TCP {}:{}", addr.ip(), addr.port());

        match timeout(self.config.tcp_timeout(), TcpStream::connect(addr)).await {
            Ok(Ok(_stream)) => {
                debug!("TCP port {} is open on {}", addr.port(), addr.ip());
                PortStatus::Open
            }
            Ok(Err(e)) => {
                let error_str = e.to_string().to_lowercase();
                if error_str.contains("refused") || error_str.contains("reset") {
                    trace!("TCP port {} is closed on {}", addr.port(), addr.ip());
                    PortStatus::Closed
                } else {
                    trace!(
                        "TCP port {} is filtered on {} (error: {})",
                        addr.port(),
                        addr.ip(),
                        e
                    );
                    PortStatus::Filtered
                }
            }
            Err(_) => {
                trace!("TCP port {} timed out on {}", addr.port(), addr.ip());
                PortStatus::Filtered
            }
        }
    }

    async fn scan_udp_port(&self, addr: SocketAddr) -> PortStatus {
        let _permit = self.semaphore.acquire().await.unwrap();

        trace!("Scanning UDP {}:{}", addr.ip(), addr.port());

        let bind_addr: SocketAddr = if addr.is_ipv4() {
            "0.0.0.0:0".parse().unwrap()
        } else {
            "[::]:0".parse().unwrap()
        };

        let socket = match UdpSocket::bind(bind_addr).await {
            Ok(s) => s,
            Err(e) => {
                warn!("Failed to bind UDP socket: {}", e);
                return PortStatus::Filtered;
            }
        };

        if let Err(e) = socket.connect(addr).await {
            warn!("Failed to connect UDP socket to {}: {}", addr, e);
            return PortStatus::Filtered;
        }

        // Simple probe
        let probe = vec![0x00];
        if let Err(e) = socket.send(&probe).await {
            warn!("Failed to send UDP probe to {}: {}", addr, e);
            return PortStatus::Filtered;
        }

        let mut buf = [0u8; 1024];
        match timeout(self.config.udp_timeout(), socket.recv(&mut buf)).await {
            Ok(Ok(_)) => {
                debug!("UDP port {} is open on {}", addr.port(), addr.ip());
                PortStatus::Open
            }
            Ok(Err(e)) => {
                let error_str = e.to_string().to_lowercase();
                if error_str.contains("refused")
                    || error_str.contains("unreachable")
                    || error_str.contains("reset")
                {
                    trace!("UDP port {} is closed on {}", addr.port(), addr.ip());
                    PortStatus::Closed
                } else {
                    trace!(
                        "UDP port {} is filtered on {} (error: {})",
                        addr.port(),
                        addr.ip(),
                        e
                    );
                    PortStatus::Filtered
                }
            }
            Err(_) => {
                trace!("UDP port {} timed out on {}", addr.port(), addr.ip());
                PortStatus::Filtered
            }
        }
    }
}
