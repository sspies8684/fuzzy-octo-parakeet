use crate::domain::{ExpectedState, PortProtocol, PortTarget};
use anyhow::{anyhow, Context, Result};
use serde::Deserialize;
use std::fs;
use std::net::{SocketAddr, ToSocketAddrs};
use std::time::Duration;

#[derive(Debug, Deserialize)]
pub struct Config {
    #[serde(default = "default_interval_secs")]
    pub default_interval_secs: u64,

    #[serde(default = "default_timeout_millis")]
    pub default_timeout_millis: u64,

    pub hosts: Vec<ConfigHost>,
}

#[derive(Debug, Deserialize)]
pub struct ConfigHost {
    pub id: String,
    pub address: String,

    #[serde(default)]
    pub interval_secs: Option<u64>,

    #[serde(default)]
    pub timeout_millis: Option<u64>,

    pub targets: Vec<ConfigTarget>,
}

#[derive(Debug, Deserialize)]
pub struct ConfigTarget {
    pub id: String,
    pub port: u16,

    #[serde(default)]
    pub protocol: Option<String>,

    #[serde(default)]
    pub expected: Option<String>,

    #[serde(default)]
    pub interval_secs: Option<u64>,

    #[serde(default)]
    pub timeout_millis: Option<u64>,
}

fn default_interval_secs() -> u64 {
    5
}

fn default_timeout_millis() -> u64 {
    800
}

pub fn load_config(path: &str) -> Result<Config> {
    let content = fs::read_to_string(path)
        .with_context(|| format!("Failed to read config file: {}", path))?;
    toml::from_str(&content)
        .with_context(|| format!("Failed to parse config TOML: {}", path))
}

pub fn build_targets(cfg: &Config) -> Result<Vec<PortTarget>> {
    let mut final_targets = Vec::new();

    for host in &cfg.hosts {
        let host_addr = resolve_host(&host.address)
            .with_context(|| format!("Could not resolve host address {}", host.address))?;

        let host_interval = host.interval_secs.unwrap_or(cfg.default_interval_secs);
        let host_timeout = host.timeout_millis.unwrap_or(cfg.default_timeout_millis);

        for t in &host.targets {
            let protocol = match t.protocol.as_deref() {
                None | Some("tcp") | Some("TCP") => PortProtocol::Tcp,
                Some("udp") | Some("UDP") => PortProtocol::Udp,
                Some(other) => return Err(anyhow!("Invalid protocol '{}'", other)),
            };

            let expected = match t.expected.as_deref() {
                None | Some("open") | Some("OPEN") => ExpectedState::Open,
                Some("closed") | Some("CLOSED") => ExpectedState::Closed,
                Some(other) => return Err(anyhow!("Invalid expected state '{}'", other)),
            };

            let interval = Duration::from_secs(t.interval_secs.unwrap_or(host_interval));

            let timeout = Duration::from_millis(t.timeout_millis.unwrap_or(host_timeout));

            let addr = SocketAddr::new(host_addr, t.port);

            final_targets.push(PortTarget {
                id: format!("{}-{}", host.id, t.id),
                addr,
                protocol,
                expected,
                interval,
                timeout,
            });
        }
    }

    Ok(final_targets)
}

fn resolve_host(host: &str) -> Result<std::net::IpAddr> {
    let mut iter = (host, 0).to_socket_addrs()?;
    iter.next()
        .map(|sock| sock.ip())
        .ok_or_else(|| anyhow!("Could not resolve hostname {}", host))
}
