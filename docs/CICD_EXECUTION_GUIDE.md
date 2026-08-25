# CI/CD Execution Guide — GitHub Actions & Live GitHub Pages E2E Pipeline

This guide details the architecture, configuration, and execution flow of the enterprise CI/CD pipeline defined in `.github/workflows/deploy-and-test.yml`.

---

## Workflow Triggers

The pipeline automatically triggers on:
- `push` to `main` or `master` branches.
- `pull_request` targeting `main` or `master` branches.
- `workflow_dispatch` (Manual trigger via GitHub UI with optional `custom_base_url` parameter).

---

## 13-Stage Pipeline Breakdown

| Stage | Name | Description |
| :--- | :--- | :--- |
| **Stage 1** | Repository Checkout | Checks out source code using `actions/checkout@v4`. |
| **Stage 2** | Dependency Installation | Installs Python 3.10, Selenium, Openpyxl, and Chrome browser binaries. |
| **Stage 3** | Build Application | Packages the static web app assets into `build_output/`. |
| **Stage 4** | Static Analysis | Lints HTML/JS assets and verifies DOCTYPE & structural integrity. |
| **Stage 5** | Deploy to GitHub Pages | Uploads artifact and deploys live via `actions/deploy-pages@v4`. |
| **Stage 6** | Wait for Deployment | Pauses to allow DNS, CDN, and global edge cache propagation. |
| **Stage 7** | Deployment Verification | Executes `verify_deployment.py` to confirm HTTP 200 and DOM readiness. |
| **Stage 8** | Run Selenium E2E Tests | Executes 440+ test cases against LIVE `BASE_URL` in headless Chrome. |
| **Stage 9** | Generate Reports | Generates interactive `execution-report.html` and `dashboard.html`. |
| **Stage 10** | Generate Excel Reports | Produces 4 Excel workbooks (`Automation_Test_Report.xlsx`, etc.). |
| **Stage 11** | Upload Artifacts | Uploads `Test Results/` folder as workflow artifact (30-day retention). |
| **Stage 12** | Publish Summary | Formats and appends markdown summary to `$GITHUB_STEP_SUMMARY`. |
| **Stage 13** | Store Historical Results | Archives test run artifacts into `historical_evidence/` by run number. |

---

## Repository & GitHub Pages Setup

### Required Repository Permissions

In GitHub Repository Settings -> **Actions** -> **General**:
- **Workflow permissions**: Read and write permissions.

In GitHub Repository Settings -> **Pages**:
- **Source**: Deploy from GitHub Actions (or branch `gh-pages`).

### Pipeline Pass/Fail Criteria

1. **Deployment Success**: Target URL returns HTTP 200 with valid CSS/JS assets.
2. **Test Suite Threshold**: Overall Selenium test pass rate must be **≥ 95%** (≤ 5% failure threshold).
