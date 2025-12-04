use std::collections::HashMap;
use std::net::SocketAddr;
use std::time::{Duration, Instant};

#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
pub enum PortProtocol {
    Tcp,
    Udp,
}

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum ExpectedState {
    Open,
    Closed,
}

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum ObservedState {
    Open,
    Closed,
    Timeout,
    Error,
    Unknown,
}

#[derive(Clone, Debug)]
pub struct PortTarget {
    pub id: String,
    pub addr: SocketAddr,
    pub protocol: PortProtocol,
    pub expected: ExpectedState,
    pub interval: Duration,
    pub timeout: Duration,
}

#[derive(Clone, Debug)]
pub struct ProbeRequest {
    pub id: String,
    pub addr: SocketAddr,
    pub protocol: PortProtocol,
    pub timeout: Duration,
}

#[derive(Clone, Debug)]
pub enum ProbeOutcome {
    Open,
    Closed,
    Timeout,
    Error,
}

#[derive(Clone, Debug)]
pub struct ProbeResult {
    pub id: String,
    pub outcome: ProbeOutcome,
}

pub struct TargetState {
    target: PortTarget,
    last_probe_at: Option<Instant>,
    next_probe_at: Instant,
    last_observed: ObservedState,
}

impl TargetState {
    pub fn new(now: Instant, target: PortTarget) -> Self {
        Self {
            target,
            last_probe_at: None,
            next_probe_at: now,
            last_observed: ObservedState::Unknown,
        }
    }

    pub fn poll_action(&self, now: Instant) -> Option<ProbeRequest> {
        if now >= self.next_probe_at {
            Some(ProbeRequest {
                id: self.target.id.clone(),
                addr: self.target.addr,
                protocol: self.target.protocol,
                timeout: self.target.timeout,
            })
        } else {
            None
        }
    }

    pub fn handle_probe_result(&mut self, now: Instant, result: ProbeResult) {
        self.last_probe_at = Some(now);
        self.next_probe_at = now + self.target.interval;
        self.last_observed = match result.outcome {
            ProbeOutcome::Open => ObservedState::Open,
            ProbeOutcome::Closed => ObservedState::Closed,
            ProbeOutcome::Timeout => ObservedState::Timeout,
            ProbeOutcome::Error => ObservedState::Error,
        };
    }

    pub fn poll_timeout(&self) -> Instant {
        self.next_probe_at
    }

    pub fn is_unexpected(&self) -> bool {
        match (self.target.expected, self.last_observed) {
            (ExpectedState::Open, ObservedState::Open) => false,
            (ExpectedState::Closed, ObservedState::Closed) => false,
            _ => true,
        }
    }

    pub fn last_observed(&self) -> ObservedState {
        self.last_observed
    }

    pub fn target(&self) -> &PortTarget {
        &self.target
    }
}

pub struct ScannerState {
    targets: HashMap<String, TargetState>,
}

impl ScannerState {
    pub fn new(now: Instant, targets: Vec<PortTarget>) -> Self {
        let mut map = HashMap::new();
        for t in targets {
            let id = t.id.clone();
            map.insert(id, TargetState::new(now, t));
        }
        Self { targets: map }
    }

    pub fn poll_timeout(&self) -> Option<Instant> {
        self.targets.values().map(|t| t.poll_timeout()).min()
    }

    pub fn poll_actions(&self, now: Instant) -> Vec<ProbeRequest> {
        self.targets
            .values()
            .filter_map(|t| t.poll_action(now))
            .collect()
    }

    pub fn handle_probe_result(&mut self, now: Instant, result: ProbeResult) {
        if let Some(t) = self.targets.get_mut(&result.id) {
            t.handle_probe_result(now, result);
        }
    }

    pub fn iter_states(&self) -> impl Iterator<Item = &TargetState> {
        self.targets.values()
    }
}

pub fn observed_state_to_f64(s: ObservedState) -> f64 {
    match s {
        ObservedState::Unknown => 0.0,
        ObservedState::Open => 1.0,
        ObservedState::Closed => 2.0,
        ObservedState::Timeout => 3.0,
        ObservedState::Error => 4.0,
    }
}
