# FocusFlow — Simple Web App

FocusFlow is a minimalist browser-based task planner designed to help you capture quick todos, mark them complete, and stay focused without needing a backend.

## Getting Started
- Open `index.html` in any modern web browser.
- Add tasks using the input field, toggle their completion status, or delete them when you're done.
- Use the filter buttons to switch between all tasks, only active items, or the list of completed wins.

## How It Works
- Tasks are stored in the browser's `localStorage`, so your list persists between visits on the same device.
- All styling (`styles.css`) and behaviour (`script.js`) are written in vanilla CSS and JavaScript for easy customization.

## Customization Ideas
- Tweak the colors or layout in `styles.css` to match your brand.
- Extend `script.js` with due dates, reminders, or data export if you need more advanced planning tools.

## GitHub Pages Deployment
1. Ensure your default branch is named `main` (or adjust the branch value in `.github/workflows/deploy.yml`).
2. Push the project and workflow file to GitHub.
3. In your repository settings, open the **Pages** tab and set **Build and deployment** → **Source** to **GitHub Actions**.
4. Merge or push to `main`; the `Deploy to GitHub Pages` workflow uploads the static site (`index.html`, `styles.css`, `script.js`) and publishes it automatically.
5. The workflow output lists the live URL. You can also find it in **Settings → Pages** once the deployment completes.

---

## Signal Prediction Market Bot

Alongside the FocusFlow UI, the repo now ships with a Python-based prediction-market bot (`prediction_market_bot.py`) that you can run in a Signal group. Everyone starts with **1000 points**, can open new markets, place bets, and the bot shares live leaderboards.

### Features
- Parimutuel payout engine with unlimited custom outcomes
- Persistent JSON storage so markets survive restarts
- CLI simulation mode for local testing
- Optional Signal connector for installations using `signal-cli-rest-api`
- Leaderboards, user stats, and portfolio summaries

### Quick Start (CLI Simulation)
```bash
cd /workspace
python3 prediction_market_bot.py --mode cli
# enter messages like:
# alice: !open Will it snow? | Yes | No
# bob: !bet MKT0001 1 200
```

State is saved (by default) to `data/prediction_state.json`. Delete the file if you want a clean slate.

### Commands
- `!open Question? | Option A | Option B` — create a new market (any user can do this)
- `!markets [status]`, `!market <id>` — list markets or inspect one
- `!bet <id> <option #> <points>` — wager your points
- `!resolve <id> <option #>` / `!cancel <id>` — settle markets or refund everyone
- `!balance`, `!leaderboard [N]`, `!portfolio`, `!stats [user]` — account utilities

### Signal Integration (optional)
1. Run [`signal-cli-rest-api`](https://github.com/bbernhard/signal-cli-rest-api) and register your bot number.
2. Install dependencies: `pip install requests`.
3. Export connection info:
   ```bash
   export SIGNAL_SERVICE_URL=http://localhost:8080
   export SIGNAL_NUMBER=+15551234567
   ```
4. Start the bot:
   ```bash
   python3 prediction_market_bot.py --mode signal
   ```
5. Add the bot number to your Signal group; use the same `!` commands as in CLI mode.
