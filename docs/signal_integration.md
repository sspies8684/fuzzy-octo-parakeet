## Signal Integration Reference

The prediction market bot uses the community-maintained
[`signal-cli-rest-api`](https://github.com/bbernhard/signal-cli-rest-api) project
as its Signal transport. That project wraps the official `signal-cli`
daemon with an HTTP/JSON interface, so the bot can:

- Poll `GET /v1/receive/<bot_number>` for new messages (long-poll/Server-Sent-Events)
- Reply via `POST /v2/send` to either group IDs or individual recipients

### Why this API?

Signal doesn’t expose an official third-party bot API. `signal-cli-rest-api` is
the de-facto SDK for hobby bots because:

- It’s actively maintained and mirrors the CLI feature set
- It supports plain HTTP calls (no DBus or Java knowledge required)
- Docker image + REST server makes deployment trivial

### Local Setup

```bash
git clone https://github.com/bbernhard/signal-cli-rest-api.git
cd signal-cli-rest-api
cp config/example.yml config/config.yml   # edit with your bot number
docker compose up -d

# register or link your bot number (follow repo README)
```

Update `.env` (or export variables) so the prediction bot knows how to reach
the Signal service:

```bash
export SIGNAL_SERVICE_URL=http://localhost:8080
export SIGNAL_NUMBER=+15551234567
```

Finally install the Python dependencies and run the bot:

```bash
pip install -r requirements.txt
python3 prediction_market_bot.py --mode signal
```

### Message Flow

1. `run_signal_mode` long-polls the REST API.
2. Each envelope’s `sourceNumber` becomes the bot “username”.
3. The bot processes commands (must start with `!`).
4. Replies are posted back to the same group or DM using `/v2/send`.

If your deployment requires proxies, mutual TLS, or message attachments, extend
`SignalRestConnector` accordingly—its methods are the integration boundary.
