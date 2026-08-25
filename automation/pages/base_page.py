import os
import time
import logging
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from automation.config.config import Config

logger = logging.getLogger("BasePage")

class BasePage:
    """Base Page Object Pattern providing reusable Selenium interaction wrappers."""

    def __init__(self, driver):
        self.driver = driver
        self.wait = WebDriverWait(driver, Config.EXPLICIT_WAIT)

    def navigate_to(self, url=None):
        target_url = url or Config.BASE_URL
        logger.info(f"Navigating to: {target_url}")
        self.driver.get(target_url)

    def find(self, locator):
        return self.wait.until(EC.presence_of_element_located(locator))

    def find_visible(self, locator):
        return self.wait.until(EC.visibility_of_element_located(locator))

    def click(self, locator):
        element = self.wait.until(EC.element_to_be_clickable(locator))
        element.click()

    def type_text(self, locator, text):
        element = self.find_visible(locator)
        element.clear()
        element.send_keys(text)

    def get_text(self, locator):
        element = self.find_visible(locator)
        return element.text.strip()

    def is_displayed(self, locator, timeout=5):
        try:
            WebDriverWait(self.driver, timeout).until(EC.visibility_of_element_located(locator))
            return True
        except (TimeoutException, NoSuchElementException):
            return False

    def capture_screenshot(self, test_id):
        Config.initialize_directories()
        filename = f"{test_id}_{int(time.time())}.png"
        filepath = os.path.join(Config.SCREENSHOTS_DIR, filename)
        try:
            self.driver.save_screenshot(filepath)
            logger.info(f"Screenshot saved: {filepath}")
            return filepath
        except Exception as e:
            logger.error(f"Failed to capture screenshot: {e}")
            return ""

    def get_browser_logs(self):
        try:
            return self.driver.get_log("browser")
        except Exception:
            return []

    def execute_js(self, script, *args):
        return self.driver.execute_script(script, *args)
