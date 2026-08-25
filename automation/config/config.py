import os

class Config:
    """Enterprise Automation Framework Configuration Manager."""
    
    # Target Base URL — ALWAYS configurable via BASE_URL environment variable.
    # Default points to the LIVE GitHub Pages deployment.
    DEFAULT_BASE_URL = "https://Lahari158.github.io/Integrated-Visual-Motor-Tool/"
    BASE_URL = os.environ.get("BASE_URL", DEFAULT_BASE_URL).rstrip("/") + "/"

    # Timeout Configuration
    IMPLICIT_WAIT = float(os.environ.get("IMPLICIT_WAIT", "10.0"))
    EXPLICIT_WAIT = float(os.environ.get("EXPLICIT_WAIT", "15.0"))
    PAGE_LOAD_TIMEOUT = float(os.environ.get("PAGE_LOAD_TIMEOUT", "30.0"))

    # Browser Execution Modes
    HEADLESS = os.environ.get("HEADLESS", "true").lower() in ("true", "1", "yes")
    WINDOW_WIDTH = int(os.environ.get("WINDOW_WIDTH", "1920"))
    WINDOW_HEIGHT = int(os.environ.get("WINDOW_HEIGHT", "1080"))

    # Path Settings
    PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    REPORTS_DIR = os.path.join(PROJECT_ROOT, "..", "Test Results")
    SCREENSHOTS_DIR = os.path.join(REPORTS_DIR, "Screenshots")
    LOGS_DIR = os.path.join(REPORTS_DIR, "Logs")
    EXCEL_DIR = os.path.join(REPORTS_DIR, "Excel")
    HTML_DIR = os.path.join(REPORTS_DIR, "HTML")
    JSON_DIR = os.path.join(REPORTS_DIR, "JSON")
    SUMMARY_DIR = os.path.join(REPORTS_DIR, "Summary")

    @classmethod
    def initialize_directories(cls):
        """Ensure all report and evidence directories exist."""
        for path in [
            cls.REPORTS_DIR, cls.SCREENSHOTS_DIR, cls.LOGS_DIR,
            cls.EXCEL_DIR, cls.HTML_DIR, cls.JSON_DIR, cls.SUMMARY_DIR
        ]:
            os.makedirs(path, exist_ok=True)
