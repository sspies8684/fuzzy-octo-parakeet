mod domain;
mod config;

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
use tracing::{error, info};
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
                update_metrics(&*s, &state_gauge, &unexpected_gauge);
            }
            _ = sleep_fut => {}
        }
    }
}

async fn run_probe(req: ProbeRequest) -> ProbeResult {
    use domain::ProbeOutcome::*;

    let outcome = match req.protocol {
        PortProtocol::Tcp => {
            let timeout = TokioDuration::from_millis(req.timeout.as_millis() as u64);
            let fut = TcpStream::connect(req.addr);
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
            let timeout = TokioDuration::from_millis(req.timeout.as_millis() as u64);
            let fut = async {
                let sock = UdpSocket::bind("0.0.0.0:0").await?;
                sock.connect(req.addr).await?;
                sock.send(&[0]).await?;
                Ok::<(), std::io::Error>(())
            };

            match tokio::time::timeout(timeout, fut).await {
                Ok(Ok(())) => Open,
                Ok(Err(_)) => Error,
                Err(_) => Timeout,
            }
        }
    };

    ProbeResult { id: req.id, outcome }
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
        update_metrics(&*s, &state_gauge, &unexpected_gauge);
    }

    let enc = TextEncoder::new();
    let families = prometheus::gather();
    let mut buf = Vec::new();
    enc.encode(&families, &mut buf).unwrap();
    String::from_utf8(buf).unwrap()
}

fn update_metrics(scanner: &ScannerState, sg: &GaugeVec, ug: &GaugeVec) {
    for ts in scanner.iter_states() {
        let t = ts.target();
        let host = t.addr.ip().to_string();
        let port = t.addr.port().to_string();
        let proto = match t.protocol {
            PortProtocol::Tcp => "tcp",
            PortProtocol::Udp => "udp",
        };
        let expected = match t.expected {
            ExpectedState::Open => "open",
            ExpectedState::Closed => "closed",
        };

        sg.with_label_values(&[&t.id, &host, &port, proto, expected])
            .set(observed_state_to_f64(ts.last_observed()));

        ug.with_label_values(&[&t.id, &host, &port, proto])
            .set(if ts.is_unexpected() { 1.0 } else { 0.0 });
    }
}

fn init_tracing() {
    use tracing_subscriber::{fmt, EnvFilter};
    let filter = EnvFilter::try_from_default_env().unwrap_or_else(|_| EnvFilter::new("info"));
    tracing_subscriber::registry().with(filter).with(fmt::layer()).init();
}
