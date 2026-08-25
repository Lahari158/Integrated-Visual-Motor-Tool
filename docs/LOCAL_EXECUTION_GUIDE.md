# Local Execution Guide — Selenium E2E Automation Framework

This guide provides instructions for configuring and running the **Integrated Visual Motor Tool** Selenium E2E Automation Suite on a local workstation.

---

## Prerequisites

1. **Python 3.9+** installed and available on `PATH`.
2. **Google Chrome** browser installed.
3. **Git** for repository interaction.

---

## Environment Setup

### 1. Clone & Navigate to Repository

```bash
git clone https://github.com/Lahari158/Integrated-Visual-Motor-Tool.git
cd Integrated-Visual-Motor-Tool
```

### 2. Create Python Virtual Environment (Recommended)

```bash
python -m venv venv
# On Windows:
venv\Scripts\activate
# On Linux/macOS:
source venv/bin/activate
```

### 3. Install Required Dependencies

```bash
pip install -r requirements.txt
# Or manually install core libraries:
pip install selenium webdriver-manager openpyxl jinja2 requests
```

---

## Running the Automated Test Suite

### 1. Run against LIVE GitHub Pages Deployment (Default)

```bash
python automation/runner.py
```

### 2. Override Target BASE_URL

To run against a specific staging or live deployment environment URL:

```bash
# Windows PowerShell:
$env:BASE_URL="https://Lahari158.github.io/Integrated-Visual-Motor-Tool/"
python automation/runner.py

# Linux / macOS:
BASE_URL="https://Lahari158.github.io/Integrated-Visual-Motor-Tool/" python automation/runner.py
```

### 3. Run in Visual (Headed) Chrome Mode for Debugging

```bash
# Windows PowerShell:
$env:HEADLESS="false"
python automation/runner.py

# Linux / macOS:
HEADLESS="false" python automation/runner.py
```

### 4. Skip Deployment Verification Check

```bash
python automation/runner.py --skip-verify
```

---

## Generated Test Artifacts

All results, logs, and reports are saved to `Test Results/`:

```
Test Results/
├── Excel/
│   ├── Automation_Test_Report.xlsx
│   ├── Failed_Test_Cases.xlsx
│   ├── Passed_Test_Cases.xlsx
│   └── Summary_Report.xlsx
├── HTML/
│   ├── execution-report.html
│   └── dashboard.html
├── Screenshots/
│   └── (Captured on failure)
├── Logs/
├── JSON/
│   └── execution-results.json
└── Summary/
    └── summary.md
```
