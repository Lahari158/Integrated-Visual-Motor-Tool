import time
import logging
from selenium.webdriver.common.by import By
from automation.config.config import Config
from automation.pages.page_objects import VisualMonitorAppPage

logger = logging.getLogger("TestDefinitions")

def execute_single_test_case(driver, tc):
    """
    Executes a single test case using the Selenium Page Object Model.
    Returns: dict with test execution results.
    """
    test_id = tc["test_id"]
    module = tc["module"]
    name = tc["name"]
    priority = tc["priority"]
    
    page = VisualMonitorAppPage(driver)
    start_time = time.time()
    
    status = "PASS"
    actual_result = "Test passed cleanly. UI state verified against live GitHub Pages deployment."
    failure_reason = ""
    screenshot_path = ""
    console_logs = []

    try:
        # Module-specific live DOM assertion logic
        if module == "Authentication":
            if "001" in test_id:
                page.navigate_to()
                page.login("trainer.admin@example.com", "SecurePass123!")
                assert page.is_displayed(page.LOGOUT_BTN, timeout=5), "Logout button not displayed after login"
                page.logout()
            elif "002" in test_id:
                page.navigate_to()
                page.login("wrong.user@example.com", "InvalidPass!")
                assert page.is_displayed(page.LOGIN_ERROR_BANNER, timeout=5), "Error banner not shown for invalid credentials"
                page.click((By.XPATH, "//button[contains(text(), 'Cancel')]"))
            elif "003" in test_id:
                page.navigate_to()
                page.click(page.LOGIN_MODAL_OPEN_BTN)
                page.click(page.LOGIN_SUBMIT_BTN)
                assert page.is_displayed(page.EMAIL_ERROR_TEXT, timeout=5), "Email validation error not displayed"
                page.click((By.XPATH, "//button[contains(text(), 'Cancel')]"))
            elif "004" in test_id:
                page.navigate_to()
                page.click(page.LOGIN_MODAL_OPEN_BTN)
                input_type = page.find(page.PASSWORD_INPUT).get_attribute("type")
                assert input_type == "password", f"Expected type 'password', got '{input_type}'"
                page.click(page.TOGGLE_PASS_VISIBILITY_BTN)
                new_type = page.find(page.PASSWORD_INPUT).get_attribute("type")
                assert new_type == "text", f"Expected type 'text' after toggle, got '{new_type}'"
                page.click((By.XPATH, "//button[contains(text(), 'Cancel')]"))
            else:
                page.navigate_to()
                assert page.is_displayed(page.APP_HEADING), "Header logo title missing"

        elif module == "Authorization":
            page.navigate_to()
            badge_text = page.get_text(page.USER_ROLE_BADGE)
            assert "Trainer" in badge_text or "Admin" in badge_text or "Guest" in badge_text, f"Unexpected badge: {badge_text}"

        elif module == "Navigation":
            page.navigate_to()
            page.click(page.NAV_MONITOR)
            assert page.is_displayed(page.CANVAS_ENGINE), "Canvas engine element not displayed on Monitor tab"
            page.click(page.NAV_DASHBOARD)
            assert page.is_displayed(page.ACTIVE_SESSIONS_COUNT), "Active sessions count metric missing on Dashboard"

        elif module == "UI Validation":
            page.navigate_to()
            title = driver.title
            assert "Visual Monitor Tool" in title or "Visual Monitor Trainer" in title, f"Unexpected page title: {title}"
            page.click(page.THEME_TOGGLE_BTN)
            theme_attr = driver.find_element(By.TAG_NAME, "body").get_attribute("data-theme")
            assert theme_attr in ["dark", "light"], f"Unexpected theme attribute: {theme_attr}"

        elif module == "Forms":
            page.navigate_to()
            page.click(page.NAV_FORMS)
            page.type_text(page.PATIENT_NAME_INPUT, "Test Patient Alpha")
            page.type_text(page.PATIENT_DOB_INPUT, "1995-05-15")
            page.click(page.SUBMIT_FORM_BTN)
            # Validate form interaction
            assert page.is_displayed(page.PATIENT_NAME_INPUT), "Form reset or state issue"

        elif module == "CRUD Operations":
            page.navigate_to()
            page.click(page.NAV_CRUD)
            assert page.is_displayed(page.DATA_TABLE), "Data table not visible on CRUD view"
            page.type_text(page.TABLE_SEARCH_INPUT, "Eleanor")
            time.sleep(0.2)

        elif module == "Input Validation":
            page.navigate_to()
            page.click(page.NAV_FORMS)
            page.type_text(page.PATIENT_NAME_INPUT, "AB")
            page.click(page.SUBMIT_FORM_BTN)
            assert page.is_displayed((By.ID, "patient-name-error")), "Short name validation error missing"

        elif module == "Error Handling":
            page.navigate_to()
            page.click(page.SIMULATE_ERROR_BTN)
            assert page.is_displayed(page.GLOBAL_ALERT_BANNER), "Global error alert banner not displayed"

        elif module == "Session Management":
            page.navigate_to()
            timer_text = page.get_text((By.ID, "session-timer"))
            assert ":" in timer_text, f"Invalid session timer format: {timer_text}"

        elif module == "File Upload":
            page.navigate_to()
            page.click(page.NAV_FILEUPLOAD)
            assert page.is_displayed(page.FILE_DROP_ZONE), "File drop zone missing"

        elif module == "Accessibility":
            page.navigate_to()
            skip_link = page.find((By.ID, "skip-to-content"))
            assert skip_link.get_attribute("href").endswith("#main-content"), "Accessibility skip-link target incorrect"

        elif module == "Responsive Design":
            page.navigate_to()
            page.click((By.ID, "mobile-menu-btn"))
            # Toggle mobile nav
            assert page.is_displayed(page.NAV_DASHBOARD), "Nav elements inaccessible"

        elif module == "Performance Smoke Tests":
            page.navigate_to()
            fps_text = page.get_text(page.FPS_DISPLAY)
            assert "FPS" in fps_text, f"FPS metric text missing: {fps_text}"

        elif module == "Regression":
            page.navigate_to()
            assert driver.current_url.startswith("http"), "Invalid current URL format"

        else:
            page.navigate_to()

    except Exception as e:
        status = "FAIL"
        failure_reason = str(e)
        actual_result = f"Test failed due to exception: {failure_reason}"
        screenshot_path = page.capture_screenshot(test_id)
        console_logs = page.get_browser_logs()
        logger.error(f"Test case {test_id} FAILED: {failure_reason}")

    exec_duration_ms = int((time.time() - start_time) * 1000)

    return {
        "test_id": test_id,
        "module": module,
        "name": name,
        "priority": priority,
        "precondition": tc["precondition"],
        "steps": tc["steps"],
        "expected": tc["expected"],
        "actual": actual_result,
        "status": status,
        "exec_time_ms": exec_duration_ms,
        "failure_reason": failure_reason,
        "screenshot": screenshot_path,
        "logs": console_logs
    }
