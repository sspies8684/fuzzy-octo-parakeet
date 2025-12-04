mod config;
mod domain;

use crate::config::{build_targets, load_config};
use crate::domain::*;

use anyhow::Result;
use axum::{routing::get, Router};
use prometheus::{register_gauge_vec, Encoder, GaugeVec, TextEncoder};
use std::net::SocketAddr;
use std::sync::Arc;
use std::time::Instant;
use tokio::net::{TcpListener, TcpStream, UdpSocket};
use tokio::sync::Mutex;
use tokio::time::{sleep_until, Duration as TokioDuration, Instant as TokioInstant};
use tracing_subscriber::layer::SubscriberExt;
use tracing_subscriber::util::SubscriberInitExt;

#[tokio::main]
async fn main() -> Result<()> {
    init_tracing();

    let config_path = std::env::args()
        .nth(1)
        .unwrap_or_else(|| "config.toml".to_string());

    let cfg = load_config(&config_path)?;
    let targets = build_targets(&cfg)?;

    let now = Instant::now();
    let scanner = Arc::new(Mutex::new(ScannerState::new(now, targets)));

    let (state_gauge, unexpected_gauge) = init_metrics();

    let scanner_clone = scanner.clone();
    let sg = state_gauge.clone();
    let ug = unexpected_gauge.clone();

    tokio::spawn(async move {
        run_scanner(scanner_clone, sg, ug).await;
    });

    let app = {
        let s = scanner.clone();
        let sg = state_gauge.clone();
        let ug = unexpected_gauge.clone();
        Router::new().route(
            "/metrics",
            get(move || metrics_handler(s.clone(), sg.clone(), ug.clone())),
        )
    };

    let addr: SocketAddr = "0.0.0.0:9100".parse().unwrap();
    let listener = TcpListener::bind(addr).await?;
    axum::serve(listener, app).await?;
    Ok(())
}

async fn run_scanner(
    scanner: Arc<Mutex<ScannerState>>,
    state_gauge: GaugeVec,
    unexpected_gauge: GaugeVec,
) {
    let (tx, mut rx) = tokio::sync::mpsc::unbounded_channel::<ProbeResult>();

    loop {
        let now = Instant::now();
        let (actions, next_timeout) = {
            let s = scanner.lock().await;
            (s.poll_actions(now), s.poll_timeout())
        };

        for action in actions {
            let tx2 = tx.clone();
            tokio::spawn(async move {
                let r = run_probe(action).await;
                let _ = tx2.send(r);
            });
        }

        let sleep_fut = if let Some(t) = next_timeout {
            let d = t
                .checked_duration_since(now)
                .unwrap_or_else(|| std::time::Duration::from_millis(10));
            sleep_until(TokioInstant::now() + TokioDuration::from_millis(d.as_millis() as u64))
        } else {
            sleep_until(TokioInstant::now() + TokioDuration::from_secs(1))
        };

        tokio::select! {
            Some(result) = rx.recv() => {
                let now = Instant::now();
                let mut s = scanner.lock().await;
                s.handle_probe_result(now, result);
                update_metrics(&s, &state_gauge, &unexpected_gauge);
            }
            _ = sleep_fut => {}
        }
    }
}

async fn run_probe(probe_request: ProbeRequest) -> ProbeResult {
    use domain::ProbeOutcome::*;

    let outcome = match probe_request.protocol {
        PortProtocol::Tcp => {
            let timeout = TokioDuration::from_millis(probe_request.timeout.as_millis() as u64);
            let fut = TcpStream::connect(probe_request.addr);
            match tokio::time::timeout(timeout, fut).await {
                Ok(Ok(_)) => Open,
                Ok(Err(e)) => {
                    if e.kind() == std::io::ErrorKind::ConnectionRefused {
                        Closed
                    } else {
                        Error
                    }
                }
                Err(_) => Timeout,
            }
        }

        PortProtocol::Udp => {
            let timeout = TokioDuration::from_millis(probe_request.timeout.as_millis() as u64);
            let result = async {
                let sock = UdpSocket::bind("0.0.0.0:0").await?;
                sock.connect(probe_request.addr).await?;
                sock.send(&[0]).await?;
                Ok::<(), std::io::Error>(())
            };

            match tokio::time::timeout(timeout, result).await {
                Ok(Ok(())) => Open,
                Ok(Err(_)) => Error,
                Err(_) => Timeout,
            }
        }
    };

    ProbeResult {
        id: probe_request.id,
        outcome,
    }
}

fn init_metrics() -> (GaugeVec, GaugeVec) {
    let state_gauge = register_gauge_vec!(
        "port_state",
        "0=unknown,1=open,2=closed,3=timeout,4=error",
        &["target", "host", "port", "protocol", "expected"]
    )
    .unwrap();

    let unexpected_gauge = register_gauge_vec!(
        "port_unexpected",
        "unexpected state indicator",
        &["target", "host", "port", "protocol"]
    )
    .unwrap();

    (state_gauge, unexpected_gauge)
}

async fn metrics_handler(
    scanner: Arc<Mutex<ScannerState>>,
    state_gauge: GaugeVec,
    unexpected_gauge: GaugeVec,
) -> String {
    {
        let s = scanner.lock().await;
        update_metrics(&s, &state_gauge, &unexpected_gauge);
    }

    let enc = TextEncoder::new();
    let families = prometheus::gather();
    let mut buf = Vec::new();
    enc.encode(&families, &mut buf).unwrap();
    String::from_utf8(buf).unwrap()
}

fn update_metrics(scanner: &ScannerState, state_gauge: &GaugeVec, unexpected_gauge: &GaugeVec) {
    for target_state in scanner.iter_states() {
        let target = target_state.target();
        let host = target.addr.ip().to_string();
        let port = target.addr.port().to_string();
        let proto = match target.protocol {
            PortProtocol::Tcp => "tcp",
            PortProtocol::Udp => "udp",
        };
        let expected = match target.expected {
            ExpectedState::Open => "open",
            ExpectedState::Closed => "closed",
        };

        state_gauge
            .with_label_values(&[&target.id, &host, &port, proto, expected])
            .set(observed_state_to_f64(target_state.last_observed()));

        unexpected_gauge
            .with_label_values(&[&target.id, &host, &port, proto])
            .set(if target_state.is_unexpected() {
                1.0
            } else {
                0.0
            });
    }
}

fn init_tracing() {
    use tracing_subscriber::{fmt, EnvFilter};
    let filter = EnvFilter::try_from_default_env().unwrap_or_else(|_| EnvFilter::new("info"));
    tracing_subscriber::registry()
        .with(filter)
        .with(fmt::layer())
        .init();
}
