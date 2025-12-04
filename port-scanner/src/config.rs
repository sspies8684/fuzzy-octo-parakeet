use anyhow::{Context, Result};
use serde::{Deserialize, Serialize};
use std::collections::HashSet;
use std::net::IpAddr;
use std::path::Path;
use std::time::Duration;

/// Main configuration structure
#[derive(Debug, Clone, Deserialize, Serialize)]
pub struct Config {
    /// Global scanner settings
    #[serde(default)]
    pub scanner: ScannerConfig,

    /// Prometheus metrics server settings
    #[serde(default)]
    pub metrics: MetricsConfig,

    /// List of hosts to scan
    pub hosts: Vec<HostConfig>,
}

/// Scanner configuration options
#[derive(Debug, Clone, Deserialize, Serialize)]
pub struct ScannerConfig {
    /// Interval between complete scan cycles (in seconds)
    #[serde(default = "default_scan_interval")]
    pub scan_interval_secs: u64,

    /// Number of parallel connections/scans
    #[serde(default = "default_parallelism")]
    pub parallelism: usize,

    /// Delay between individual port scans (in milliseconds)
    #[serde(default = "default_scan_delay_ms")]
    pub scan_delay_ms: u64,

    /// TCP connection timeout (in milliseconds)
    #[serde(default = "default_tcp_timeout_ms")]
    pub tcp_timeout_ms: u64,

    /// UDP probe timeout (in milliseconds)
    #[serde(default = "default_udp_timeout_ms")]
    pub udp_timeout_ms: u64,

    /// DNS resolution timeout (in seconds)
    #[serde(default = "default_dns_timeout_secs")]
    pub dns_timeout_secs: u64,
}

impl Default for ScannerConfig {
    fn default() -> Self {
        Self {
            scan_interval_secs: default_scan_interval(),
            parallelism: default_parallelism(),
            scan_delay_ms: default_scan_delay_ms(),
            tcp_timeout_ms: default_tcp_timeout_ms(),
            udp_timeout_ms: default_udp_timeout_ms(),
            dns_timeout_secs: default_dns_timeout_secs(),
        }
    }
}

impl ScannerConfig {
    pub fn scan_interval(&self) -> Duration {
        Duration::from_secs(self.scan_interval_secs)
    }

    pub fn scan_delay(&self) -> Duration {
        Duration::from_millis(self.scan_delay_ms)
    }

    pub fn tcp_timeout(&self) -> Duration {
        Duration::from_millis(self.tcp_timeout_ms)
    }

    pub fn udp_timeout(&self) -> Duration {
        Duration::from_millis(self.udp_timeout_ms)
    }

    pub fn dns_timeout(&self) -> Duration {
        Duration::from_secs(self.dns_timeout_secs)
    }
}

/// Prometheus metrics server configuration
#[derive(Debug, Clone, Deserialize, Serialize)]
pub struct MetricsConfig {
    /// Address to bind the metrics server to
    #[serde(default = "default_metrics_address")]
    pub address: String,

    /// Port for the metrics server
    #[serde(default = "default_metrics_port")]
    pub port: u16,
}

impl Default for MetricsConfig {
    fn default() -> Self {
        Self {
            address: default_metrics_address(),
            port: default_metrics_port(),
        }
    }
}

/// Configuration for a single host to scan
#[derive(Debug, Clone, Deserialize, Serialize)]
pub struct HostConfig {
    /// DNS name of the host (used for identification and resolution)
    pub name: String,

    /// Optional IP address to bypass DNS resolution
    #[serde(default)]
    pub ip: Option<IpAddr>,

    /// Optional description/label for this host
    #[serde(default)]
    pub description: Option<String>,

    /// TCP ports that should be open
    #[serde(default)]
    pub expected_tcp_ports: Vec<PortSpec>,

    /// UDP ports that should be open
    #[serde(default)]
    pub expected_udp_ports: Vec<PortSpec>,

    /// TCP ports to scan (defaults to expected_tcp_ports if not specified)
    #[serde(default)]
    pub scan_tcp_ports: Option<Vec<PortSpec>>,

    /// UDP ports to scan (defaults to expected_udp_ports if not specified)
    #[serde(default)]
    pub scan_udp_ports: Option<Vec<PortSpec>>,

    /// Whether to scan all common ports (1-1024) in addition to specified ports
    #[serde(default)]
    pub scan_common_ports: bool,

    /// Whether to scan all ports (1-65535) - use with caution
    #[serde(default)]
    pub scan_all_ports: bool,
}

impl HostConfig {
    /// Get the set of TCP ports to scan
    pub fn tcp_ports_to_scan(&self) -> HashSet<u16> {
        let mut ports = HashSet::new();

        // Add explicitly specified scan ports or expected ports
        let port_specs = self
            .scan_tcp_ports
            .as_ref()
            .unwrap_or(&self.expected_tcp_ports);

        for spec in port_specs {
            ports.extend(spec.expand());
        }

        // Add common ports if requested
        if self.scan_common_ports {
            ports.extend(1..=1024);
        }

        // Add all ports if requested
        if self.scan_all_ports {
            ports.extend(1..=65535);
        }

        ports
    }

    /// Get the set of UDP ports to scan
    pub fn udp_ports_to_scan(&self) -> HashSet<u16> {
        let mut ports = HashSet::new();

        // Add explicitly specified scan ports or expected ports
        let port_specs = self
            .scan_udp_ports
            .as_ref()
            .unwrap_or(&self.expected_udp_ports);

        for spec in port_specs {
            ports.extend(spec.expand());
        }

        // Add common ports if requested (for UDP, we use a smaller set)
        if self.scan_common_ports {
            // Common UDP ports
            ports.extend([53, 67, 68, 69, 123, 137, 138, 161, 162, 500, 514, 520, 1194]);
        }

        // Add all ports if requested
        if self.scan_all_ports {
            ports.extend(1..=65535);
        }

        ports
    }

    /// Get the set of expected open TCP ports
    pub fn expected_tcp_open(&self) -> HashSet<u16> {
        self.expected_tcp_ports
            .iter()
            .flat_map(|spec| spec.expand())
            .collect()
    }

    /// Get the set of expected open UDP ports
    pub fn expected_udp_open(&self) -> HashSet<u16> {
        self.expected_udp_ports
            .iter()
            .flat_map(|spec| spec.expand())
            .collect()
    }
}

/// Specification for port(s) - can be a single port, a range, or a list
#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(untagged)]
pub enum PortSpec {
    /// Single port number
    Single(u16),
    /// Port range as "start-end"
    Range(String),
    /// List of ports
    List(Vec<u16>),
}

impl PortSpec {
    /// Expand this port specification into individual port numbers
    pub fn expand(&self) -> Vec<u16> {
        match self {
            PortSpec::Single(port) => vec![*port],
            PortSpec::Range(range) => {
                if let Some((start, end)) = range.split_once('-') {
                    if let (Ok(start), Ok(end)) = (start.parse::<u16>(), end.parse::<u16>()) {
                        return (start..=end).collect();
                    }
                }
                vec![]
            }
            PortSpec::List(ports) => ports.clone(),
        }
    }
}

// Default value functions for serde
fn default_scan_interval() -> u64 {
    60 // 1 minute
}

fn default_parallelism() -> usize {
    100
}

fn default_scan_delay_ms() -> u64 {
    10
}

fn default_tcp_timeout_ms() -> u64 {
    3000 // 3 seconds
}

fn default_udp_timeout_ms() -> u64 {
    5000 // 5 seconds
}

fn default_dns_timeout_secs() -> u64 {
    5
}

fn default_metrics_address() -> String {
    "0.0.0.0".to_string()
}

fn default_metrics_port() -> u16 {
    9090
}

impl Config {
    /// Load configuration from a file
    pub fn load<P: AsRef<Path>>(path: P) -> Result<Self> {
        let path = path.as_ref();
        let content = std::fs::read_to_string(path)
            .with_context(|| format!("Failed to read config file: {}", path.display()))?;

        // Determine format based on file extension
        let config = if path.extension().map_or(false, |ext| ext == "yaml" || ext == "yml") {
            serde_yaml::from_str(&content)
                .with_context(|| format!("Failed to parse YAML config: {}", path.display()))?
        } else if path.extension().map_or(false, |ext| ext == "toml") {
            toml::from_str(&content)
                .with_context(|| format!("Failed to parse TOML config: {}", path.display()))?
        } else {
            // Try YAML first, then TOML
            serde_yaml::from_str(&content)
                .or_else(|_| toml::from_str(&content))
                .with_context(|| format!("Failed to parse config file: {}", path.display()))?
        };

        Ok(config)
    }

    /// Validate the configuration
    pub fn validate(&self) -> Result<()> {
        if self.hosts.is_empty() {
            anyhow::bail!("No hosts configured for scanning");
        }

        for host in &self.hosts {
            if host.name.is_empty() {
                anyhow::bail!("Host name cannot be empty");
            }

            // Ensure at least some ports are configured for scanning
            if host.expected_tcp_ports.is_empty()
                && host.expected_udp_ports.is_empty()
                && host.scan_tcp_ports.is_none()
                && host.scan_udp_ports.is_none()
                && !host.scan_common_ports
                && !host.scan_all_ports
            {
                anyhow::bail!(
                    "Host '{}' has no ports configured for scanning",
                    host.name
                );
            }
        }

        if self.scanner.parallelism == 0 {
            anyhow::bail!("Parallelism must be greater than 0");
        }

        Ok(())
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_port_spec_single() {
        let spec = PortSpec::Single(80);
        assert_eq!(spec.expand(), vec![80]);
    }

    #[test]
    fn test_port_spec_range() {
        let spec = PortSpec::Range("80-83".to_string());
        assert_eq!(spec.expand(), vec![80, 81, 82, 83]);
    }

    #[test]
    fn test_port_spec_list() {
        let spec = PortSpec::List(vec![22, 80, 443]);
        assert_eq!(spec.expand(), vec![22, 80, 443]);
    }
}
