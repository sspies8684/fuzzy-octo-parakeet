# Port Scanner

A continuous network port scanner written in Rust that monitors TCP and UDP ports and exports Prometheus metrics. It detects unexpected open ports (security risks) and unexpected closed ports (service outages).

## Features

- **Continuous Scanning**: Runs continuously, scanning hosts at configurable intervals
- **TCP & UDP Support**: Scans both TCP and UDP ports with protocol-specific probes
- **Prometheus Metrics**: Exposes metrics for monitoring and alerting
- **Flexible Configuration**: YAML or TOML configuration files
- **DNS Resolution**: Resolve hostnames or use direct IP addresses
- **Configurable Parallelism**: Control concurrent scans for performance tuning
- **Port Ranges**: Specify individual ports, ranges, or lists

## Installation

### Build from Source

```bash
# Clone or navigate to the project
cd port-scanner

# Build release binary
cargo build --release

# Binary will be at ./target/release/port-scanner
```

### Run with Cargo

```bash
cargo run --release -- --config config.yaml
```

## Usage

```bash
# Run with default config (config.yaml)
./port-scanner

# Specify custom config file
./port-scanner --config /path/to/config.yaml

# Override settings via CLI
./port-scanner --interval 30 --parallelism 200 --metrics-port 9091

# Run a single scan and exit
./port-scanner --once

# Validate configuration without scanning
./port-scanner --dry-run

# Enable debug logging
./port-scanner --log-level debug
```

### Command Line Options

| Option | Description | Default |
|--------|-------------|---------|
| `-c, --config` | Path to configuration file | `config.yaml` |
| `--interval` | Override scan interval (seconds) | From config |
| `--parallelism` | Override number of concurrent scans | From config |
| `--metrics-port` | Override metrics server port | From config |
| `--log-level` | Log level (trace, debug, info, warn, error) | `info` |
| `--once` | Run single scan cycle and exit | `false` |
| `--dry-run` | Validate config without scanning | `false` |

## Configuration

Configuration can be in YAML or TOML format. See `config.yaml` or `config.example.toml` for examples.

### Scanner Settings

```yaml
scanner:
  # Interval between complete scan cycles (seconds)
  scan_interval_secs: 60
  
  # Number of parallel connections/scans
  parallelism: 100
  
  # Delay between scan batches (milliseconds)
  scan_delay_ms: 10
  
  # TCP connection timeout (milliseconds)
  tcp_timeout_ms: 3000
  
  # UDP probe timeout (milliseconds)
  udp_timeout_ms: 5000
  
  # DNS resolution timeout (seconds)
  dns_timeout_secs: 5
```

### Metrics Server

```yaml
metrics:
  address: "0.0.0.0"
  port: 9090
```

### Host Configuration

```yaml
hosts:
  - name: "server.example.com"      # DNS name (required)
    ip: 192.168.1.10                # Optional: bypass DNS resolution
    description: "Web server"        # Optional: description
    
    # Ports that SHOULD be open (if closed, alert)
    expected_tcp_ports:
      - 22                           # Single port
      - 80
      - 443
      - "8080-8089"                  # Port range
      - [3000, 3001, 3002]           # List of ports
    
    expected_udp_ports:
      - 53
    
    # Optional: Additional ports to scan (beyond expected)
    scan_tcp_ports:
      - "1-1024"
    
    # Scan common privileged ports (1-1024)
    scan_common_ports: false
    
    # Scan ALL ports (1-65535) - use with caution!
    scan_all_ports: false
```

## Prometheus Metrics

The scanner exposes metrics at `http://localhost:9090/metrics` (configurable).

### Available Metrics

| Metric | Labels | Description |
|--------|--------|-------------|
| `port_scanner_tcp_port_open` | host, ip, port | TCP port status (1=open, 0=closed/filtered) |
| `port_scanner_udp_port_open` | host, ip, port | UDP port status (1=open, 0=closed/filtered) |
| `port_scanner_tcp_unexpected_open` | host, ip, port | TCP port open but shouldn't be (1=alert) |
| `port_scanner_tcp_unexpected_closed` | host, ip, port | TCP port closed but should be open (1=alert) |
| `port_scanner_udp_unexpected_open` | host, ip, port | UDP port open but shouldn't be (1=alert) |
| `port_scanner_udp_unexpected_closed` | host, ip, port | UDP port closed but should be open (1=alert) |
| `port_scanner_tcp_unexpected_open_count` | host | Count of unexpected open TCP ports |
| `port_scanner_tcp_unexpected_closed_count` | host | Count of unexpected closed TCP ports |
| `port_scanner_udp_unexpected_open_count` | host | Count of unexpected open UDP ports |
| `port_scanner_udp_unexpected_closed_count` | host | Count of unexpected closed UDP ports |
| `port_scanner_scan_cycles_total` | host | Total scan cycles completed |
| `port_scanner_scan_errors_total` | host, error_type | Scan errors |
| `port_scanner_last_scan_timestamp_seconds` | host | Unix timestamp of last scan |
| `port_scanner_scan_duration_seconds` | host | Duration of last scan |

### Example Prometheus Alerts

```yaml
groups:
  - name: port_scanner
    rules:
      # Alert on unexpected open ports (security risk)
      - alert: UnexpectedPortOpen
        expr: port_scanner_tcp_unexpected_open == 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Unexpected open TCP port detected"
          description: "Host {{ $labels.host }} has unexpected open TCP port {{ $labels.port }}"

      # Alert on expected ports being closed (service outage)
      - alert: ExpectedPortClosed
        expr: port_scanner_tcp_unexpected_closed == 1
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Expected port is closed"
          description: "Host {{ $labels.host }} TCP port {{ $labels.port }} should be open but is closed"

      # Alert if scanning is failing
      - alert: ScanNotRunning
        expr: time() - port_scanner_last_scan_timestamp_seconds > 300
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Port scanning has stopped"
          description: "No scan results for host {{ $labels.host }} in the last 5 minutes"
```

### Grafana Dashboard

Example queries for Grafana:

```promql
# Number of unexpected open TCP ports per host
sum by (host) (port_scanner_tcp_unexpected_open)

# List all unexpected open ports
port_scanner_tcp_unexpected_open == 1

# Services that should be running but aren't
port_scanner_tcp_unexpected_closed == 1

# Scan duration over time
port_scanner_scan_duration_seconds
```

## How It Works

### TCP Scanning

TCP scanning uses standard connection attempts:
- **Open**: Connection established successfully
- **Closed**: Connection refused (RST received)
- **Filtered**: Timeout (no response, likely filtered by firewall)

### UDP Scanning

UDP scanning is inherently less reliable:
- Sends protocol-specific probes for known services (DNS, NTP, SNMP)
- **Open**: Received a response
- **Closed**: ICMP Port Unreachable received
- **Filtered**: No response (could be open or filtered)

## Performance Considerations

- **Parallelism**: Higher values scan faster but use more resources and may trigger rate limiting
- **Timeouts**: Lower timeouts scan faster but may miss slow-responding services
- **Scan Interval**: Balance between freshness and network load
- **Full Port Scan**: Scanning all 65535 ports takes significant time; use sparingly

### Recommended Settings

| Environment | Parallelism | Interval | Timeout |
|-------------|-------------|----------|---------|
| LAN (trusted network) | 500-1000 | 60s | 1000ms |
| WAN (remote servers) | 50-100 | 300s | 5000ms |
| Security Audit | 100-200 | N/A | 3000ms |

## Security Notes

- **Permissions**: May require elevated privileges for certain scans
- **Network Policy**: Ensure port scanning is permitted in your network
- **Rate Limiting**: Some hosts/networks may rate-limit or block scanners
- **UDP Scanning**: Requires ability to receive ICMP responses

## License

MIT License
