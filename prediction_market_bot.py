#!/usr/bin/env python3
"""
Signal Prediction Market Bot.

Core features:
- Anyone can open a market with custom outcomes.
- Currency is "points"; every new participant starts with 1000 points.
- Users can bet on outcomes, resolve markets, and view leaderboards.
- State is saved to disk (JSON) so activity survives restarts.
- Optional Signal connector for environments using signal-cli-rest-api.

Run `python prediction_market_bot.py --help` for usage.
"""

from __future__ import annotations

import argparse
import json
import os
import textwrap
import threading
import time
from dataclasses import dataclass, field, asdict
from pathlib import Path
from typing import Dict, List, Optional, Tuple

try:
    import requests  # type: ignore
except ImportError:  # pragma: no cover - optional dependency
    requests = None


STARTING_BALANCE = 1000


@dataclass
class Bet:
    user: str
    option_index: int
    amount: int
    timestamp: float = field(default_factory=time.time)

    def to_dict(self) -> Dict:
        return asdict(self)

    @classmethod
    def from_dict(cls, data: Dict) -> "Bet":
        return cls(
            user=data["user"],
            option_index=data["option_index"],
            amount=data["amount"],
            timestamp=data.get("timestamp", time.time()),
        )


@dataclass
class Market:
    market_id: str
    question: str
    creator: str
    options: List[str]
    status: str = "open"  # open, resolved, cancelled
    bets: List[Bet] = field(default_factory=list)
    winning_option: Optional[int] = None
    created_at: float = field(default_factory=time.time)
    resolved_at: Optional[float] = None

    def to_dict(self) -> Dict:
        return {
            "market_id": self.market_id,
            "question": self.question,
            "creator": self.creator,
            "options": self.options,
            "status": self.status,
            "bets": [b.to_dict() for b in self.bets],
            "winning_option": self.winning_option,
            "created_at": self.created_at,
            "resolved_at": self.resolved_at,
        }

    @classmethod
    def from_dict(cls, data: Dict) -> "Market":
        return cls(
            market_id=data["market_id"],
            question=data["question"],
            creator=data["creator"],
            options=data["options"],
            status=data.get("status", "open"),
            bets=[Bet.from_dict(b) for b in data.get("bets", [])],
            winning_option=data.get("winning_option"),
            created_at=data.get("created_at", time.time()),
            resolved_at=data.get("resolved_at"),
        )

    def total_pool(self) -> int:
        return sum(b.amount for b in self.bets)

    def pool_by_option(self) -> List[int]:
        totals = [0] * len(self.options)
        for bet in self.bets:
            totals[bet.option_index] += bet.amount
        return totals


class PredictionMarketBot:
    """Core prediction market logic independent of Signal."""

    def __init__(self, state_path: Path, starting_balance: int = STARTING_BALANCE):
        self.state_path = state_path
        self.state_path.parent.mkdir(parents=True, exist_ok=True)
        self.starting_balance = starting_balance
        self.lock = threading.Lock()
        self.users: Dict[str, Dict] = {}
        self.markets: Dict[str, Market] = {}
        self.next_market_id = 1
        self._load_state()

    # ------------------------------------------------------------------ #
    # Persistence
    # ------------------------------------------------------------------ #
    def _load_state(self) -> None:
        if not self.state_path.exists():
            self._save_state()
            return

        data = json.loads(self.state_path.read_text())
        self.users = data.get("users", {})
        self.markets = {
            market_id: Market.from_dict(payload)
            for market_id, payload in data.get("markets", {}).items()
        }
        self.next_market_id = data.get("next_market_id", 1)

    def _save_state(self) -> None:
        data = {
            "users": self.users,
            "markets": {mid: market.to_dict() for mid, market in self.markets.items()},
            "next_market_id": self.next_market_id,
            "updated_at": time.time(),
        }
        self.state_path.write_text(json.dumps(data, indent=2, sort_keys=True))

    # ------------------------------------------------------------------ #
    # Public API
    # ------------------------------------------------------------------ #
    def process_command(self, user: str, message: str) -> str:
        """Parse and execute a chat command, returning a reply string."""
        user = user.strip()
        message = message.strip()
        if not user:
            return "Missing user identifier."
        if not message.startswith("!"):
            return "Commands must start with `!`. Try `!help`."

        self._ensure_user(user)
        parts = message[1:].strip().split()
        if not parts:
            return "Empty command. Try `!help`."

        cmd = parts[0].lower()
        argline = message[1 + len(cmd):].strip()

        handlers = {
            "help": self._cmd_help,
            "commands": self._cmd_help,
            "open": self._cmd_open,
            "markets": self._cmd_markets,
            "market": self._cmd_market_detail,
            "bet": self._cmd_bet,
            "balance": self._cmd_balance,
            "leaderboard": self._cmd_leaderboard,
            "resolve": self._cmd_resolve,
            "cancel": self._cmd_cancel,
            "portfolio": self._cmd_portfolio,
            "stats": self._cmd_stats,
        }

        handler = handlers.get(cmd)
        if not handler:
            return f"Unknown command `{cmd}`. Try `!help`."

        with self.lock:
            response = handler(user, argline)
            self._save_state()
            return response

    # ------------------------------------------------------------------ #
    # Command handlers
    # ------------------------------------------------------------------ #
    def _cmd_help(self, *_: str) -> str:
        return textwrap.dedent(
            """
            Prediction Market Bot Commands:
            • !open Question? | Option A | Option B  → create a new market
            • !markets [open|resolved|cancelled]    → list markets
            • !market <id>                          → show details for a market
            • !bet <id> <option #> <points>         → bet points on an outcome
            • !resolve <id> <option #>              → resolve a market
            • !cancel <id>                          → refund everyone and close
            • !balance                              → view your points
            • !portfolio                            → see your open bets
            • !leaderboard [N]                      → top N balances (default 10)
            • !stats [user]                         → lifetime stats (default self)
            All users start with 1000 points. Bets are parimutuel: winners split the pool.
            """
        ).strip()

    def _cmd_open(self, user: str, argline: str) -> str:
        if "|" not in argline:
            return "Use `!open Question? | Yes | No` with at least two options."

        parts = [segment.strip() for segment in argline.split("|")]
        question = parts[0]
        options = [opt for opt in parts[1:] if opt]

        if not question:
            return "Please provide a question or market description."
        if len(options) < 2:
            return "Provide at least two outcome options."
        if any(len(opt) > 40 for opt in options):
            return "Options must be 40 characters or fewer."

        market_id = f"MKT{self.next_market_id:04d}"
        self.next_market_id += 1
        market = Market(
            market_id=market_id,
            question=question,
            creator=user,
            options=options,
        )
        self.markets[market_id] = market

        user_stats = self.users[user]
        user_stats["markets_created"] = user_stats.get("markets_created", 0) + 1

        return textwrap.dedent(
            f"""
            Market {market_id} created by {user}:
            {question}
            Options:
            {self._format_options(options)}
            Place bets with `!bet {market_id} <option #> <points>`.
            """
        ).strip()

    def _cmd_markets(self, _user: str, argline: str) -> str:
        status_filter = argline.lower() if argline else "open"
        valid = {"open", "resolved", "cancelled", "all"}
        if status_filter and status_filter not in valid:
            return f"Status must be one of {', '.join(sorted(valid))}."

        markets = self.markets.values()
        if status_filter != "all":
            markets = [m for m in markets if m.status == status_filter]

        if not markets:
            return f"No {status_filter} markets yet."

        lines = []
        for market in sorted(markets, key=lambda m: m.created_at, reverse=True):
            pool = market.total_pool()
            status_str = market.status
            if market.status == "resolved" and market.winning_option is not None:
                status_str += f" → {market.options[market.winning_option]}"
            lines.append(
                f"{market.market_id} [{status_str}] {market.question} (Pool: {pool} pts)"
            )

        return "\n".join(lines)

    def _cmd_market_detail(self, _user: str, argline: str) -> str:
        market_id = argline.strip().upper()
        if not market_id:
            return "Usage: `!market <id>`."
        market = self.markets.get(market_id)
        if not market:
            return f"Market {market_id} not found."

        pools = market.pool_by_option()
        total = market.total_pool()
        option_lines = []
        for idx, option in enumerate(market.options):
            pooled = pools[idx]
            pct = f"{(pooled / total * 100):.1f}%" if total else "—"
            option_lines.append(f"{idx + 1}. {option} — {pooled} pts ({pct})")

        status_line = f"Status: {market.status}"
        if market.status == "resolved" and market.winning_option is not None:
            status_line += f" (Winner: {market.options[market.winning_option]})"

        return textwrap.dedent(
            f"""
            {market.market_id} • {market.question}
            Creator: {market.creator}
            {status_line}
            Pool: {total} pts
            Options:
            {self._format_options(option_lines, bullet=False)}
            """
        ).strip()

    def _cmd_bet(self, user: str, argline: str) -> str:
        parts = argline.split()
        if len(parts) != 3:
            return "Usage: `!bet <market> <option #> <points>`."

        market_id, option_raw, amount_raw = parts
        market_id = market_id.upper()
        market = self.markets.get(market_id)
        if not market:
            return f"Market {market_id} not found."
        if market.status != "open":
            return f"Market {market_id} is {market.status}."

        try:
            option_idx = int(option_raw) - 1
        except ValueError:
            return "Option must be a number."
        if option_idx < 0 or option_idx >= len(market.options):
            return f"Option must be between 1 and {len(market.options)}."

        try:
            amount = int(amount_raw)
        except ValueError:
            return "Bet amount must be an integer."
        if amount <= 0:
            return "Bet amount must be positive."

        balance = self.users[user]["balance"]
        if amount > balance:
            return f"Insufficient points. You have {balance}."

        self.users[user]["balance"] -= amount
        self.users[user]["bets_placed"] = self.users[user].get("bets_placed", 0) + 1
        market.bets.append(Bet(user=user, option_index=option_idx, amount=amount))

        pools = market.pool_by_option()
        total = market.total_pool()
        option_pool = pools[option_idx]
        implied_pct = f"{(option_pool / total * 100):.1f}%" if total else "—"
        option_name = market.options[option_idx]
        return (
            f"Bet accepted: {amount} pts on `{option_name}` in {market.market_id}.\n"
            f"Pool on this option: {option_pool} pts ({implied_pct}). Total pool: {total} pts."
        )

    def _cmd_balance(self, user: str, _argline: str) -> str:
        balance = self.users[user]["balance"]
        return f"{user} balance: {balance} pts."

    def _cmd_leaderboard(self, _user: str, argline: str) -> str:
        limit = 10
        if argline:
            try:
                limit = max(1, min(50, int(argline)))
            except ValueError:
                return "Leaderboard limit must be a number."

        rows = sorted(
            self.users.items(), key=lambda item: item[1].get("balance", 0), reverse=True
        )
        lines = ["Points Leaderboard:"]
        for idx, (username, stats) in enumerate(rows[:limit], start=1):
            lines.append(f"{idx}. {username} — {stats.get('balance', 0)} pts")
        return "\n".join(lines)

    def _cmd_resolve(self, user: str, argline: str) -> str:
        parts = argline.split()
        if len(parts) != 2:
            return "Usage: `!resolve <market> <option #>`."

        market_id, option_raw = parts
        market_id = market_id.upper()
        market = self.markets.get(market_id)
        if not market:
            return f"Market {market_id} not found."
        if market.status != "open":
            return f"Market {market_id} is already {market.status}."

        try:
            winning_idx = int(option_raw) - 1
        except ValueError:
            return "Option must be a number."
        if winning_idx < 0 or winning_idx >= len(market.options):
            return f"Option must be between 1 and {len(market.options)}."

        summary = self._resolve_market(market, winning_idx)
        return f"{market_id} resolved: {summary}"

    def _cmd_cancel(self, user: str, argline: str) -> str:
        market_id = argline.strip().upper()
        if not market_id:
            return "Usage: `!cancel <market>`."
        market = self.markets.get(market_id)
        if not market:
            return f"Market {market_id} not found."
        if market.status != "open":
            return f"Market {market_id} is already {market.status}."

        for bet in market.bets:
            self.users[bet.user]["balance"] += bet.amount
        market.status = "cancelled"
        market.resolved_at = time.time()
        return f"{market_id} cancelled. All bets refunded."

    def _cmd_portfolio(self, user: str, _argline: str) -> str:
        entries = []
        for market in self.markets.values():
            if market.status != "open":
                continue
            for bet in market.bets:
                if bet.user == user:
                    option = market.options[bet.option_index]
                    entries.append(
                        f"{market.market_id} {market.question} → {option} ({bet.amount} pts)"
                    )
        if not entries:
            return "No open bets."
        return "Open bets:\n" + "\n".join(entries)

    def _cmd_stats(self, user: str, argline: str) -> str:
        target = argline.strip() or user
        if target not in self.users:
            return f"No user named {target}."
        stats = self.users[target]
        parts = [
            f"Stats for {target}:",
            f"• Balance: {stats.get('balance', 0)} pts",
            f"• Markets created: {stats.get('markets_created', 0)}",
            f"• Bets placed: {stats.get('bets_placed', 0)}",
            f"• Lifetime winnings: {stats.get('lifetime_winnings', 0)} pts",
        ]
        return "\n".join(parts)

    # ------------------------------------------------------------------ #
    # Helpers
    # ------------------------------------------------------------------ #
    def _ensure_user(self, user: str) -> None:
        if user not in self.users:
            self.users[user] = {
                "balance": self.starting_balance,
                "lifetime_winnings": 0,
                "markets_created": 0,
                "bets_placed": 0,
                "created_at": time.time(),
            }

    def _format_options(self, options: List[str], bullet: bool = True) -> str:
        if bullet:
            return "\n".join(f"{idx + 1}. {opt}" for idx, opt in enumerate(options))
        return "\n".join(options)

    def _resolve_market(self, market: Market, winning_idx: int) -> str:
        winners = [bet for bet in market.bets if bet.option_index == winning_idx]
        total_pool = market.total_pool()
        winners_total = sum(b.amount for b in winners)

        if winners_total == 0:
            # Refund everyone because there were no correct bets.
            for bet in market.bets:
                self.users[bet.user]["balance"] += bet.amount
            market.status = "resolved"
            market.winning_option = winning_idx
            market.resolved_at = time.time()
            return (
                f"{market.options[winning_idx]} won, but no one bet on it. "
                "All points refunded."
            )

        remainder = total_pool
        payouts: List[Tuple[str, int]] = []
        for bet in winners:
            payout = (bet.amount * total_pool) // winners_total
            payouts.append((bet.user, payout))
            remainder -= payout

        # Distribute leftover points (from floor division) to top contributors.
        payouts.sort(key=lambda item: item[1], reverse=True)
        idx = 0
        while remainder > 0 and payouts:
            user, payout = payouts[idx % len(payouts)]
            payouts[idx % len(payouts)] = (user, payout + 1)
            remainder -= 1
            idx += 1

        # Apply payouts and track winnings.
        for user, payout in payouts:
            self.users[user]["balance"] += payout
            self.users[user]["lifetime_winnings"] = (
                self.users[user].get("lifetime_winnings", 0) + payout
            )

        market.status = "resolved"
        market.winning_option = winning_idx
        market.resolved_at = time.time()

        winners_text = ", ".join(f"{user} (+{payout} pts)" for user, payout in payouts)
        return f"{market.options[winning_idx]} wins. Payouts: {winners_text or 'N/A'}."


# ---------------------------------------------------------------------- #
# Signal connector (optional, requires signal-cli-rest-api)
# ---------------------------------------------------------------------- #
class SignalRestConnector:
    """
    Minimal connector for installations that expose signal-cli via the REST API:
      https://github.com/bbernhard/signal-cli-rest-api

    This class polls for new messages and sends replies back to each conversation.
    """

    def __init__(self, service_url: str, phone_number: str):
        if requests is None:
            raise RuntimeError("`requests` is required for Signal mode. `pip install requests`.")
        self.service_url = service_url.rstrip("/")
        self.phone_number = phone_number

    def receive(self, timeout: int = 30) -> List[Dict]:
        url = f"{self.service_url}/v1/receive/{self.phone_number}"
        resp = requests.get(url, params={"timeout": timeout}, timeout=timeout + 5)
        resp.raise_for_status()
        data = resp.json()
        return data if isinstance(data, list) else []

    def reply(self, destination: Dict, message: str) -> None:
        payload = {
            "message": message,
            "number": self.phone_number,
        }
        if "groupInfo" in destination and destination["groupInfo"]:
            payload["recipients"] = []
            payload["group_id"] = destination["groupInfo"]["groupId"]
        else:
            payload["recipients"] = [destination["source"]["number"]]

        url = f"{self.service_url}/v2/send"
        resp = requests.post(url, json=payload, timeout=15)
        resp.raise_for_status()


def run_cli_mode(bot: PredictionMarketBot) -> None:
    print("Prediction Market Bot CLI")
    print("Enter messages as `username: !command ...` (Ctrl+C to exit).")
    while True:
        try:
            line = input("> ").strip()
        except (KeyboardInterrupt, EOFError):
            print("\nBye!")
            break
        if not line:
            continue
        if ":" not in line:
            print("Format: username: !command ...")
            continue
        user, message = [part.strip() for part in line.split(":", 1)]
        response = bot.process_command(user, message)
        print(response)


def run_signal_mode(bot: PredictionMarketBot, connector: SignalRestConnector, poll: int = 15) -> None:
    print("Starting Signal mode. Press Ctrl+C to stop.")
    try:
        while True:
            envelopes = connector.receive(timeout=poll)
            for envelope in envelopes:
                data_message = envelope.get("dataMessage")
                if not data_message:
                    continue
                source = envelope.get("sourceNumber") or envelope.get("source", {}).get("number")
                message = data_message.get("message", "")
                if not message:
                    continue
                reply_text = bot.process_command(source or "unknown", message)
                connector.reply(envelope, reply_text)
    except KeyboardInterrupt:
        print("\nSignal bot stopped.")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Signal Prediction Market Bot")
    parser.add_argument(
        "--state",
        default="data/prediction_state.json",
        help="Path to the persistent JSON state file (default: data/prediction_state.json)",
    )
    parser.add_argument(
        "--mode",
        choices=["cli", "signal"],
        default="cli",
        help="Run in CLI simulation mode or Signal connector mode.",
    )
    parser.add_argument(
        "--signal-url",
        default=os.environ.get("SIGNAL_SERVICE_URL", "http://localhost:8080"),
        help="signal-cli-rest-api base URL (Signal mode only).",
    )
    parser.add_argument(
        "--signal-number",
        default=os.environ.get("SIGNAL_NUMBER"),
        help="Bot phone number registered with signal-cli (Signal mode only).",
    )
    parser.add_argument("--poll", type=int, default=15, help="Signal poll timeout (seconds).")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    bot = PredictionMarketBot(Path(args.state))

    if args.mode == "cli":
        run_cli_mode(bot)
        return

    if not args.signal_number:
        raise SystemExit("Signal mode requires --signal-number or SIGNAL_NUMBER.")
    connector = SignalRestConnector(args.signal_url, args.signal_number)
    run_signal_mode(bot, connector, poll=args.poll)


if __name__ == "__main__":
    main()
