import sys
import time
import argparse
import logging
from automation.config.config import Config
from automation.utils.driver_factory import DriverFactory
from automation.data.test_data_generator import generate_all_test_cases
from automation.tests.test_definitions import execute_single_test_case
from automation.utils.excel_reporter import ExcelReporter
from automation.utils.html_reporter import HTMLReporter
from automation.utils.summary_generator import SummaryGenerator
from automation.utils.verify_deployment import verify_live_deployment

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("TestRunner")

def run_suite(skip_verify=False):
    """Executes the full 440+ Selenium E2E test suite against LIVE BASE_URL."""
    logger.info("=" * 60)
    logger.info("PHASE 7 — LIVE PRODUCTION SELENIUM E2E EXECUTION SUITE")
    logger.info("=" * 60)
    logger.info(f"Target BASE_URL: {Config.BASE_URL}")

    # Stage 7: Deployment Verification
    if not skip_verify:
        logger.info("Executing Stage 7: Deployment Verification...")
        if not verify_live_deployment():
            logger.error("Deployment Verification FAILED. Aborting Selenium Test Execution.")
            sys.exit(1)

    test_cases = generate_all_test_cases()
    logger.info(f"Loaded {len(test_cases)} executable test cases across 14 modules.")

    driver = DriverFactory.get_driver()
    results = []
    start_time = time.time()

    try:
        for idx, tc in enumerate(test_cases, 1):
            logger.info(f"[{idx}/{len(test_cases)}] Running {tc['test_id']}: {tc['name']}")
            res = execute_single_test_case(driver, tc)
            
            # Retry logic on failure
            if res["status"] == "FAIL":
                logger.warning(f"  Retry attempt 1 for {tc['test_id']}...")
                res_retry = execute_single_test_case(driver, tc)
                if res_retry["status"] == "PASS":
                    res = res_retry

            results.append(res)
    finally:
        driver.quit()
        logger.info("Selenium WebDriver closed.")

    duration_sec = time.time() - start_time
    total = len(results)
    passed = sum(1 for r in results if r["status"] == "PASS")
    failed = sum(1 for r in results if r["status"] == "FAIL")
    pass_pct = (passed / total * 100) if total > 0 else 0.0

    logger.info("=" * 60)
    logger.info(f"EXECUTION SUMMARY: Total={total} | Passed={passed} | Failed={failed} | Pass Rate={pass_pct:.2f}%")
    logger.info("=" * 60)

    # Generate Reports
    logger.info("Generating Excel Reports...")
    ExcelReporter(results).generate_all_excel_reports()

    logger.info("Generating HTML & JSON Reports...")
    HTMLReporter(results, duration_sec).generate_all_html_reports()

    logger.info("Generating Markdown Summary & GitHub Step Summary...")
    SummaryGenerator(results, duration_sec).generate_summary()

    # Pass/Fail Threshold Evaluation (Pass Rate must be >= 95%)
    if pass_pct < 95.0:
        logger.error(f"❌ PIPELINE EVALUATION FAILED: Pass rate {pass_pct:.2f}% is below required 95.0% threshold.")
        sys.exit(1)
    else:
        logger.info(f"✅ PIPELINE EVALUATION PASSED: Pass rate {pass_pct:.2f}% meets required 95.0% threshold.")
        sys.exit(0)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Selenium E2E Test Suite Runner")
    parser.add_argument("--skip-verify", action="store_true", help="Skip initial HTTP deployment verification check")
    args = parser.parse_args()

    run_suite(skip_verify=args.skip_verify)
