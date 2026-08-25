# Troubleshooting Guide — Live CI/CD & Selenium E2E Automation

This guide provides diagnostic procedures and resolution steps for potential runtime issues.

---

## 1. Mandatory Rule: Live Target vs Localhost

> [!CAUTION]
> **RULE**: Selenium tests must **NEVER** run against `localhost` or `127.0.0.1` in CI/CD pipelines.

### Symptom:
`DeploymentVerifier` throws `MANDATORY RULE VIOLATION`.

### Solution:
Verify `BASE_URL` environment variable points to your live GitHub Pages URL:
`https://<github-username>.github.io/<repository-name>/`

---

## 2. Stage 7: Deployment Verification Timeout / HTTP 404

### Symptom:
Stage 7 fails with `HTTP Error 404: Not Found` or `Connection Refused`.

### Root Causes & Fixes:
1. **GitHub Pages Initial Build Delay**: First-time deployments can take up to 2-3 minutes to propagate DNS records.
   - *Fix*: Re-run the workflow or increase `max_retries` in `verify_deployment.py`.
2. **Repository Pages Configuration**:
   - Navigate to GitHub Repo -> Settings -> Pages. Ensure Source is set to **GitHub Actions**.

---

## 3. Chrome / WebDriver Initialization Errors

### Symptom:
`SessionNotCreatedException: Could not start a new session` or `WebDriverException: chrome not reachable`.

### Resolution:
Ensure the required headless flags are passed in `driver_factory.py`:
```python
chrome_options.add_argument("--headless=new")
chrome_options.add_argument("--no-sandbox")
chrome_options.add_argument("--disable-dev-shm-usage")
```

---

## 4. Pipeline Failure Threshold (< 95% Pass Rate)

### Symptom:
GitHub Action step `Stage 8` fails with `PIPELINE EVALUATION FAILED: Pass rate below 95%`.

### Resolution:
1. Download the `live-selenium-e2e-test-evidence` artifact zip from the run.
2. Open `Excel/Failed_Test_Cases.xlsx` or `HTML/execution-report.html`.
3. Inspect captured failure screenshots in `Screenshots/` and console logs in `Logs/`.
4. Fix underlying UI/DOM bugs or update locator mappings in `page_objects.py`.
