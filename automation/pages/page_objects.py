from selenium.webdriver.common.by import By
from automation.pages.base_page import BasePage

class VisualMonitorAppPage(BasePage):
    """Page Object for the Integrated Visual Motor Tool Web Application."""

    # Locators
    APP_HEADING = (By.ID, "app-heading")
    NAV_DASHBOARD = (By.ID, "nav-dashboard")
    NAV_MONITOR = (By.ID, "nav-monitor")
    NAV_CRUD = (By.ID, "nav-crud")
    NAV_FORMS = (By.ID, "nav-forms")
    NAV_FILEUPLOAD = (By.ID, "nav-fileupload")
    NAV_PROFILE = (By.ID, "nav-profile")
    NAV_SETTINGS = (By.ID, "nav-settings")
    THEME_TOGGLE_BTN = (By.ID, "theme-toggle-btn")
    USER_ROLE_BADGE = (By.ID, "user-role-badge")

    # Auth Locators
    LOGIN_MODAL_OPEN_BTN = (By.ID, "login-modal-open-btn")
    LOGOUT_BTN = (By.ID, "logout-btn")
    AUTH_MODAL = (By.ID, "auth-modal")
    EMAIL_INPUT = (By.ID, "email")
    PASSWORD_INPUT = (By.ID, "password")
    LOGIN_SUBMIT_BTN = (By.ID, "login-btn")
    TOGGLE_PASS_VISIBILITY_BTN = (By.ID, "toggle-password-visibility")
    LOGIN_ERROR_BANNER = (By.ID, "login-error-banner")
    EMAIL_ERROR_TEXT = (By.ID, "email-error")

    # Dashboard Locators
    SYSTEM_STATUS_BADGE = (By.ID, "system-status-badge")
    ACTIVE_SESSIONS_COUNT = (By.ID, "active-sessions-count")
    ACCURACY_RATE_VAL = (By.ID, "accuracy-rate-val")
    FPS_DISPLAY = (By.ID, "fps-display")
    TOTAL_ITEMS_BADGE = (By.ID, "total-items-badge")
    SIMULATE_ERROR_BTN = (By.ID, "simulate-error-btn")
    GLOBAL_ALERT_BANNER = (By.ID, "global-alert-banner")

    # Visual Monitor Locators
    CANVAS_ENGINE = (By.ID, "visual-motor-canvas")
    CANVAS_START_BTN = (By.ID, "canvas-start-btn")

    # CRUD Locators
    DATA_TABLE = (By.ID, "data-table")
    ADD_ITEM_BTN = (By.ID, "add-item-btn")
    TABLE_SEARCH_INPUT = (By.ID, "table-search-input")
    ITEM_TITLE_INPUT = (By.ID, "item-title")
    ITEM_SCORE_INPUT = (By.ID, "item-score")
    SAVE_ITEM_BTN = (By.ID, "save-item-btn")

    # Assessment Form Locators
    PATIENT_NAME_INPUT = (By.ID, "patient-name")
    PATIENT_DOB_INPUT = (By.ID, "patient-dob")
    MODULE_SELECT = (By.ID, "training-module-select")
    SUBMIT_FORM_BTN = (By.ID, "submit-form-btn")
    FORM_SUCCESS_BANNER = (By.ID, "form-success-banner")

    # File Upload Locators
    FILE_DROP_ZONE = (By.ID, "file-drop-zone")
    FILE_UPLOAD_INPUT = (By.ID, "file-upload-input")
    UPLOADED_FILE_NAME = (By.ID, "uploaded-file-name")
    UPLOAD_SUBMIT_BTN = (By.ID, "upload-submit-btn")
    UPLOAD_STATUS_MSG = (By.ID, "upload-status-msg")

    # Profile Locators
    PROFILE_USER_NAME = (By.ID, "profile-user-name")
    PROFILE_USER_EMAIL = (By.ID, "profile-user-email")

    def login(self, email, password):
        self.click(self.LOGIN_MODAL_OPEN_BTN)
        self.type_text(self.EMAIL_INPUT, email)
        self.type_text(self.PASSWORD_INPUT, password)
        self.click(self.LOGIN_SUBMIT_BTN)

    def logout(self):
        if self.is_displayed(self.LOGOUT_BTN):
            self.click(self.LOGOUT_BTN)
