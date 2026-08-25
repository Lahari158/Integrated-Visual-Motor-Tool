"""
Deployment Verification Script for CI/CD Stage 7.
Verifies target deployment availability before running Selenium E2E tests.
"""

import sys
import time
import urllib.request
import urllib.error
import logging
from automation.config.config import Config

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("DeploymentVerifier")

def verify_live_deployment(url=None, max_retries=12, retry_delay=5):
    target_url = url or Config.BASE_URL
    logger.info(f"Initiating Deployment Verification against LIVE URL: {target_url}")

    if "localhost" in target_url or "127.0.0.1" in target_url:
        logger.error("MANDATORY RULE VIOLATION: Selenium/Verification target CANNOT be localhost!")
        sys.exit(1)

    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) DeploymentVerifier/1.0'}

    for attempt in range(1, max_retries + 1):
        try:
            req = urllib.request.Request(target_url, headers=headers)
            with urllib.request.urlopen(req, timeout=10) as response:
                status_code = response.getcode()
                content = response.read().decode('utf-8', errors='ignore')

                if status_code == 200:
                    logger.info(f"[Attempt {attempt}/{max_retries}] HTTP 200 OK received from {target_url}")
                    
                    # Verify key page components
                    checks = {
                        "HTML doctype": "<!DOCTYPE html>" in content.lower() or "<html" in content.lower(),
                        "Visual Monitor Title": "Visual Monitor" in content or "Integrated Visual Motor" in content,
                        "Styles/CSS Asset": "<style" in content.lower() or ".css" in content.lower(),
                        "JavaScript Asset": "<script" in content.lower(),
                        "App Header Component": "app-heading" in content or "Visual Monitor Trainer" in content
                    }

                    all_passed = True
                    for check_name, passed in checks.items():
                        if passed:
                            logger.info(f"  ✓ Verification Check Passed: {check_name}")
                        else:
                            logger.warning(f"  ✗ Verification Check Warning: {check_name}")
                            all_passed = False

                    if all_passed:
                        logger.info("🎉 DEPLOYMENT VERIFICATION SUCCESSFUL! Application is LIVE and healthy.")
                        return True
        except urllib.error.HTTPError as e:
            logger.warning(f"[Attempt {attempt}/{max_retries}] HTTP Error: {e.code} {e.reason}")
        except urllib.error.URLError as e:
            logger.warning(f"[Attempt {attempt}/{max_retries}] Connection Failed: {e.reason}")
        except Exception as e:
            logger.warning(f"[Attempt {attempt}/{max_retries}] Verification Exception: {e}")

        if attempt < max_retries:
            logger.info(f"Waiting {retry_delay}s before retrying deployment check...")
            time.sleep(retry_delay)

    logger.error(f"❌ DEPLOYMENT VERIFICATION FAILED: Unable to verify LIVE deployment at {target_url}")
    return False

if __name__ == "__main__":
    success = verify_live_deployment()
    if not success:
        sys.exit(1)
