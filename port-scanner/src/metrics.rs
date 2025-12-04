use anyhow::Result;
use http_body_util::Full;
use hyper::body::Bytes;
use hyper::server::conn::http1;
use hyper::service::service_fn;
use hyper::{Request, Response, StatusCode};
use hyper_util::rt::TokioIo;
use prometheus::{GaugeVec, IntCounterVec, IntGaugeVec, Opts, Registry, TextEncoder};
use std::collections::HashSet;
use std::net::SocketAddr;
use std::sync::Arc;
use tokio::net::TcpListener;
use tokio::sync::RwLock;
use tracing::{debug, error, info};

use crate::config::MetricsConfig;
use crate::scanner::HostScanResult;

/// Prometheus metrics for port scanning
pub struct Metrics {
    registry: Registry,

    /// Gauge indicating if a TCP port is open (1) or not (0)
    /// Labels: host, ip, port
    tcp_port_open: GaugeVec,

    /// Gauge indicating if a UDP port is open (1) or not (0)
    /// Labels: host, ip, port
    udp_port_open: GaugeVec,

    /// Gauge for TCP ports that are open but shouldn't be (1 = unexpected open)
    /// Labels: host, ip, port
    tcp_unexpected_open: IntGaugeVec,

    /// Gauge for TCP ports that are closed but should be open (1 = unexpected closed)
    /// Labels: host, ip, port
    tcp_unexpected_closed: IntGaugeVec,

    /// Gauge for UDP ports that are open but shouldn't be (1 = unexpected open)
    /// Labels: host, ip, port
    udp_unexpected_open: IntGaugeVec,

    /// Gauge for UDP ports that are closed but should be open (1 = unexpected closed)
    /// Labels: host, ip, port
    udp_unexpected_closed: IntGaugeVec,

    /// Counter for total scan cycles completed
    /// Labels: host
    scan_cycles_total: IntCounterVec,

    /// Counter for scan errors
    /// Labels: host, error_type
    scan_errors_total: IntCounterVec,

    /// Gauge for last scan timestamp (Unix epoch seconds)
    /// Labels: host
    last_scan_timestamp: GaugeVec,

    /// Gauge for scan duration in seconds
    /// Labels: host
    scan_duration_seconds: GaugeVec,

    /// Count of unexpected open TCP ports per host
    /// Labels: host
    tcp_unexpected_open_count: IntGaugeVec,

    /// Count of unexpected closed TCP ports per host
    /// Labels: host
    tcp_unexpected_closed_count: IntGaugeVec,

    /// Count of unexpected open UDP ports per host
    /// Labels: host
    udp_unexpected_open_count: IntGaugeVec,

    /// Count of unexpected closed UDP ports per host
    /// Labels: host
    udp_unexpected_closed_count: IntGaugeVec,
}

impl Metrics {
    /// Create a new Metrics instance
    pub fn new() -> Result<Self> {
        let registry = Registry::new();

        // TCP port open gauge
        let tcp_port_open = GaugeVec::new(
            Opts::new("port_scanner_tcp_port_open", "TCP port open status (1=open, 0=closed/filtered)")
                .namespace("port_scanner"),
            &["host", "ip", "port"],
        )?;
        registry.register(Box::new(tcp_port_open.clone()))?;

        // UDP port open gauge
        let udp_port_open = GaugeVec::new(
            Opts::new("port_scanner_udp_port_open", "UDP port open status (1=open, 0=closed/filtered)")
                .namespace("port_scanner"),
            &["host", "ip", "port"],
        )?;
        registry.register(Box::new(udp_port_open.clone()))?;

        // TCP unexpected open
        let tcp_unexpected_open = IntGaugeVec::new(
            Opts::new(
                "port_scanner_tcp_unexpected_open",
                "TCP port is open but should be closed (1=unexpected open)",
            )
            .namespace("port_scanner"),
            &["host", "ip", "port"],
        )?;
        registry.register(Box::new(tcp_unexpected_open.clone()))?;

        // TCP unexpected closed
        let tcp_unexpected_closed = IntGaugeVec::new(
            Opts::new(
                "port_scanner_tcp_unexpected_closed",
                "TCP port is closed but should be open (1=unexpected closed)",
            )
            .namespace("port_scanner"),
            &["host", "ip", "port"],
        )?;
        registry.register(Box::new(tcp_unexpected_closed.clone()))?;

        // UDP unexpected open
        let udp_unexpected_open = IntGaugeVec::new(
            Opts::new(
                "port_scanner_udp_unexpected_open",
                "UDP port is open but should be closed (1=unexpected open)",
            )
            .namespace("port_scanner"),
            &["host", "ip", "port"],
        )?;
        registry.register(Box::new(udp_unexpected_open.clone()))?;

        // UDP unexpected closed
        let udp_unexpected_closed = IntGaugeVec::new(
            Opts::new(
                "port_scanner_udp_unexpected_closed",
                "UDP port is closed but should be open (1=unexpected closed)",
            )
            .namespace("port_scanner"),
            &["host", "ip", "port"],
        )?;
        registry.register(Box::new(udp_unexpected_closed.clone()))?;

        // Scan cycles counter
        let scan_cycles_total = IntCounterVec::new(
            Opts::new("port_scanner_scan_cycles_total", "Total number of completed scan cycles")
                .namespace("port_scanner"),
            &["host"],
        )?;
        registry.register(Box::new(scan_cycles_total.clone()))?;

        // Scan errors counter
        let scan_errors_total = IntCounterVec::new(
            Opts::new("port_scanner_scan_errors_total", "Total number of scan errors")
                .namespace("port_scanner"),
            &["host", "error_type"],
        )?;
        registry.register(Box::new(scan_errors_total.clone()))?;

        // Last scan timestamp
        let last_scan_timestamp = GaugeVec::new(
            Opts::new(
                "port_scanner_last_scan_timestamp_seconds",
                "Unix timestamp of last completed scan",
            )
            .namespace("port_scanner"),
            &["host"],
        )?;
        registry.register(Box::new(last_scan_timestamp.clone()))?;

        // Scan duration
        let scan_duration_seconds = GaugeVec::new(
            Opts::new("port_scanner_scan_duration_seconds", "Duration of last scan in seconds")
                .namespace("port_scanner"),
            &["host"],
        )?;
        registry.register(Box::new(scan_duration_seconds.clone()))?;

        // Aggregate counts
        let tcp_unexpected_open_count = IntGaugeVec::new(
            Opts::new(
                "port_scanner_tcp_unexpected_open_count",
                "Count of TCP ports that are unexpectedly open",
            )
            .namespace("port_scanner"),
            &["host"],
        )?;
        registry.register(Box::new(tcp_unexpected_open_count.clone()))?;

        let tcp_unexpected_closed_count = IntGaugeVec::new(
            Opts::new(
                "port_scanner_tcp_unexpected_closed_count",
                "Count of TCP ports that are unexpectedly closed",
            )
            .namespace("port_scanner"),
            &["host"],
        )?;
        registry.register(Box::new(tcp_unexpected_closed_count.clone()))?;

        let udp_unexpected_open_count = IntGaugeVec::new(
            Opts::new(
                "port_scanner_udp_unexpected_open_count",
                "Count of UDP ports that are unexpectedly open",
            )
            .namespace("port_scanner"),
            &["host"],
        )?;
        registry.register(Box::new(udp_unexpected_open_count.clone()))?;

        let udp_unexpected_closed_count = IntGaugeVec::new(
            Opts::new(
                "port_scanner_udp_unexpected_closed_count",
                "Count of UDP ports that are unexpectedly closed",
            )
            .namespace("port_scanner"),
            &["host"],
        )?;
        registry.register(Box::new(udp_unexpected_closed_count.clone()))?;

        Ok(Self {
            registry,
            tcp_port_open,
            udp_port_open,
            tcp_unexpected_open,
            tcp_unexpected_closed,
            udp_unexpected_open,
            udp_unexpected_closed,
            scan_cycles_total,
            scan_errors_total,
            last_scan_timestamp,
            scan_duration_seconds,
            tcp_unexpected_open_count,
            tcp_unexpected_closed_count,
            udp_unexpected_open_count,
            udp_unexpected_closed_count,
        })
    }

    /// Update metrics from a scan result
    pub fn update_from_scan_result(&self, result: &HostScanResult, duration_secs: f64) {
        let host = &result.host_name;
        let ip = result.ip_address.to_string();

        // Update TCP port status
        for (&port, &status) in &result.tcp_results {
            let port_str = port.to_string();
            let value = if status == crate::scanner::PortStatus::Open {
                1.0
            } else {
                0.0
            };
            self.tcp_port_open
                .with_label_values(&[host, &ip, &port_str])
                .set(value);
        }

        // Update UDP port status
        for (&port, &status) in &result.udp_results {
            let port_str = port.to_string();
            let value = if status == crate::scanner::PortStatus::Open {
                1.0
            } else {
                0.0
            };
            self.udp_port_open
                .with_label_values(&[host, &ip, &port_str])
                .set(value);
        }

        // Update TCP unexpected open
        self.update_unexpected_ports(
            &self.tcp_unexpected_open,
            host,
            &ip,
            &result.tcp_unexpected_open,
            &result.tcp_results.keys().copied().collect(),
        );

        // Update TCP unexpected closed
        self.update_unexpected_ports(
            &self.tcp_unexpected_closed,
            host,
            &ip,
            &result.tcp_unexpected_closed,
            &result.tcp_results.keys().copied().collect(),
        );

        // Update UDP unexpected open
        self.update_unexpected_ports(
            &self.udp_unexpected_open,
            host,
            &ip,
            &result.udp_unexpected_open,
            &result.udp_results.keys().copied().collect(),
        );

        // Update UDP unexpected closed
        self.update_unexpected_ports(
            &self.udp_unexpected_closed,
            host,
            &ip,
            &result.udp_unexpected_closed,
            &result.udp_results.keys().copied().collect(),
        );

        // Update aggregate counts
        self.tcp_unexpected_open_count
            .with_label_values(&[host])
            .set(result.tcp_unexpected_open.len() as i64);

        self.tcp_unexpected_closed_count
            .with_label_values(&[host])
            .set(result.tcp_unexpected_closed.len() as i64);

        self.udp_unexpected_open_count
            .with_label_values(&[host])
            .set(result.udp_unexpected_open.len() as i64);

        self.udp_unexpected_closed_count
            .with_label_values(&[host])
            .set(result.udp_unexpected_closed.len() as i64);

        // Update scan metadata
        self.scan_cycles_total.with_label_values(&[host]).inc();

        self.last_scan_timestamp.with_label_values(&[host]).set(
            std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .unwrap()
                .as_secs_f64(),
        );

        self.scan_duration_seconds
            .with_label_values(&[host])
            .set(duration_secs);
    }

    /// Helper to update unexpected port metrics
    fn update_unexpected_ports(
        &self,
        gauge: &IntGaugeVec,
        host: &str,
        ip: &str,
        unexpected_ports: &HashSet<u16>,
        all_scanned_ports: &HashSet<u16>,
    ) {
        // Set 1 for unexpected ports, 0 for others
        for &port in all_scanned_ports {
            let port_str = port.to_string();
            let value = if unexpected_ports.contains(&port) { 1 } else { 0 };
            gauge.with_label_values(&[host, ip, &port_str]).set(value);
        }
    }

    /// Record a scan error
    pub fn record_scan_error(&self, host: &str, error_type: &str) {
        self.scan_errors_total
            .with_label_values(&[host, error_type])
            .inc();
    }

    /// Get the encoded metrics for Prometheus scraping
    pub fn encode(&self) -> Result<String> {
        let encoder = TextEncoder::new();
        let metric_families = self.registry.gather();
        let mut buffer = String::new();
        encoder.encode_utf8(&metric_families, &mut buffer)?;
        Ok(buffer)
    }
}

/// Metrics server state
struct MetricsServerState {
    metrics: Arc<RwLock<Metrics>>,
}

/// Handle HTTP requests for metrics
async fn handle_request(
    req: Request<hyper::body::Incoming>,
    state: Arc<MetricsServerState>,
) -> Result<Response<Full<Bytes>>, hyper::Error> {
    let path = req.uri().path();

    match path {
        "/metrics" => {
            let metrics = state.metrics.read().await;
            match metrics.encode() {
                Ok(body) => {
                    let response = Response::builder()
                        .status(StatusCode::OK)
                        .header("Content-Type", "text/plain; charset=utf-8")
                        .body(Full::new(Bytes::from(body)))
                        .unwrap();
                    Ok(response)
                }
                Err(e) => {
                    error!("Failed to encode metrics: {}", e);
                    let response = Response::builder()
                        .status(StatusCode::INTERNAL_SERVER_ERROR)
                        .body(Full::new(Bytes::from(format!("Error: {}", e))))
                        .unwrap();
                    Ok(response)
                }
            }
        }
        "/health" | "/healthz" => {
            let response = Response::builder()
                .status(StatusCode::OK)
                .body(Full::new(Bytes::from("OK")))
                .unwrap();
            Ok(response)
        }
        "/" => {
            let html = r#"<!DOCTYPE html>
<html>
<head><title>Port Scanner Metrics</title></head>
<body>
<h1>Port Scanner Metrics</h1>
<p><a href="/metrics">Metrics</a></p>
<p><a href="/health">Health</a></p>
</body>
</html>"#;
            let response = Response::builder()
                .status(StatusCode::OK)
                .header("Content-Type", "text/html; charset=utf-8")
                .body(Full::new(Bytes::from(html)))
                .unwrap();
            Ok(response)
        }
        _ => {
            let response = Response::builder()
                .status(StatusCode::NOT_FOUND)
                .body(Full::new(Bytes::from("Not Found")))
                .unwrap();
            Ok(response)
        }
    }
}

/// Start the Prometheus metrics HTTP server
pub async fn start_metrics_server(
    config: MetricsConfig,
    metrics: Arc<RwLock<Metrics>>,
) -> Result<()> {
    let addr: SocketAddr = format!("{}:{}", config.address, config.port)
        .parse()
        .expect("Invalid metrics server address");

    let state = Arc::new(MetricsServerState { metrics });

    let listener = TcpListener::bind(addr).await?;
    info!("Prometheus metrics server listening on http://{}", addr);

    loop {
        let (stream, remote_addr) = match listener.accept().await {
            Ok(conn) => conn,
            Err(e) => {
                error!("Failed to accept connection: {}", e);
                continue;
            }
        };

        debug!("Accepted connection from {}", remote_addr);

        let io = TokioIo::new(stream);
        let state = Arc::clone(&state);

        tokio::spawn(async move {
            let service = service_fn(move |req| {
                let state = Arc::clone(&state);
                async move { handle_request(req, state).await }
            });

            if let Err(e) = http1::Builder::new().serve_connection(io, service).await {
                error!("Error serving connection: {}", e);
            }
        });
    }
}
