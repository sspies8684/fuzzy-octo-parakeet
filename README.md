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
