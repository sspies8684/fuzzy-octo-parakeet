mod config;
mod metrics;
mod scanner;

use anyhow::{Context, Result};
use clap::Parser;
use std::path::PathBuf;
use std::sync::Arc;
use std::time::Instant;
use tokio::sync::RwLock;
use tracing::{error, info, warn, Level};
use tracing_subscriber::EnvFilter;

use crate::config::Config;
use crate::metrics::{start_metrics_server, Metrics};
use crate::scanner::Scanner;

/// Continuous network port scanner with Prometheus metrics
#[derive(Parser, Debug)]
#[command(name = "port-scanner")]
#[command(author, version, about, long_about = None)]
struct Args {
    /// Path to configuration file (YAML or TOML)
    #[arg(short, long, default_value = "config.yaml")]
    config: PathBuf,

    /// Override scan interval (seconds)
    #[arg(long)]
    interval: Option<u64>,

    /// Override parallelism (number of concurrent scans)
    #[arg(long)]
    parallelism: Option<usize>,

    /// Override metrics server port
    #[arg(long)]
    metrics_port: Option<u16>,

    /// Log level (trace, debug, info, warn, error)
    #[arg(long, default_value = "info")]
    log_level: String,

    /// Run a single scan and exit (don't run continuously)
    #[arg(long)]
    once: bool,

    /// Dry run - load config and validate but don't scan
    #[arg(long)]
    dry_run: bool,
}

#[tokio::main]
async fn main() -> Result<()> {
    let args = Args::parse();

    // Initialize logging
    let log_level = args.log_level.parse::<Level>().unwrap_or(Level::INFO);
    tracing_subscriber::fmt()
        .with_env_filter(
            EnvFilter::from_default_env()
                .add_directive(format!("port_scanner={}", log_level).parse().unwrap()),
        )
        .init();

    info!("Port Scanner starting up");

    // Load configuration
    let mut config = Config::load(&args.config)
        .with_context(|| format!("Failed to load config from {:?}", args.config))?;

    // Apply command-line overrides
    if let Some(interval) = args.interval {
        config.scanner.scan_interval_secs = interval;
    }
    if let Some(parallelism) = args.parallelism {
        config.scanner.parallelism = parallelism;
    }
    if let Some(port) = args.metrics_port {
        config.metrics.port = port;
    }

    // Validate configuration
    config.validate()?;

    info!(
        "Configuration loaded: {} hosts, scan interval: {}s, parallelism: {}",
        config.hosts.len(),
        config.scanner.scan_interval_secs,
        config.scanner.parallelism
    );

    for host in &config.hosts {
        let tcp_ports = host.tcp_ports_to_scan().len();
        let udp_ports = host.udp_ports_to_scan().len();
        info!(
            "  Host: {} - {} TCP ports, {} UDP ports",
            host.name, tcp_ports, udp_ports
        );
    }

    if args.dry_run {
        info!("Dry run mode - configuration is valid, exiting");
        return Ok(());
    }

    // Initialize metrics
    let metrics = Arc::new(RwLock::new(Metrics::new()?));

    // Start metrics server in background
    let metrics_clone = Arc::clone(&metrics);
    let metrics_config = config.metrics.clone();
    tokio::spawn(async move {
        if let Err(e) = start_metrics_server(metrics_config, metrics_clone).await {
            error!("Metrics server error: {}", e);
        }
    });

    // Create scanner
    let scanner = Scanner::new(config.scanner.clone()).await?;

    // Main scanning loop
    let scan_interval = config.scanner.scan_interval();

    if args.once {
        info!("Running single scan cycle...");
        run_scan_cycle(&scanner, &config, &metrics).await;
        info!("Single scan complete");
    } else {
        info!("Starting continuous scanning loop");
        loop {
            run_scan_cycle(&scanner, &config, &metrics).await;

            info!(
                "Scan cycle complete. Waiting {} seconds until next cycle...",
                scan_interval.as_secs()
            );
            tokio::time::sleep(scan_interval).await;
        }
    }

    Ok(())
}

/// Run a complete scan cycle for all configured hosts
async fn run_scan_cycle(scanner: &Scanner, config: &Config, metrics: &Arc<RwLock<Metrics>>) {
    for host in &config.hosts {
        let start = Instant::now();
        info!("Scanning host: {}", host.name);

        match scanner.scan_host(host).await {
            Ok(result) => {
                let duration = start.elapsed();
                let duration_secs = duration.as_secs_f64();

                // Log results
                info!(
                    "Host {} scan complete in {:.2}s:",
                    result.host_name, duration_secs
                );
                info!(
                    "  TCP: {} ports scanned, {} open",
                    result.tcp_results.len(),
                    result
                        .tcp_results
                        .values()
                        .filter(|s| **s == scanner::PortStatus::Open)
                        .count()
                );
                info!(
                    "  UDP: {} ports scanned, {} open",
                    result.udp_results.len(),
                    result
                        .udp_results
                        .values()
                        .filter(|s| **s == scanner::PortStatus::Open)
                        .count()
                );

                // Log unexpected states
                if !result.tcp_unexpected_open.is_empty() {
                    warn!(
                        "  TCP unexpected OPEN: {:?}",
                        result.tcp_unexpected_open
                    );
                }
                if !result.tcp_unexpected_closed.is_empty() {
                    warn!(
                        "  TCP unexpected CLOSED: {:?}",
                        result.tcp_unexpected_closed
                    );
                }
                if !result.udp_unexpected_open.is_empty() {
                    warn!(
                        "  UDP unexpected OPEN: {:?}",
                        result.udp_unexpected_open
                    );
                }
                if !result.udp_unexpected_closed.is_empty() {
                    warn!(
                        "  UDP unexpected CLOSED: {:?}",
                        result.udp_unexpected_closed
                    );
                }

                // Update metrics
                let metrics_guard = metrics.read().await;
                metrics_guard.update_from_scan_result(&result, duration_secs);
            }
            Err(e) => {
                error!("Failed to scan host {}: {}", host.name, e);
                let metrics_guard = metrics.read().await;
                metrics_guard.record_scan_error(&host.name, "scan_failed");
            }
        }
    }
}
