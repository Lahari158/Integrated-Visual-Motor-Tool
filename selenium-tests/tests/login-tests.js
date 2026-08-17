const { Builder, By, until } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const assert = require('assert');

describe('Web E2E Login & Functionality Suite - Visual Monitor Trainer', function () {
    this.timeout(60000);
    let driver;

    const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';

    beforeEach(async function () {
        const options = new chrome.Options();
        options.addArguments('--headless=new');
        options.addArguments('--no-sandbox');
        options.addArguments('--disable-dev-shm-usage');
        options.addArguments('--window-size=1920,1080');

        driver = await new Builder()
            .forBrowser('chrome')
            .setChromeOptions(options)
            .build();
    });

    afterEach(async function () {
        if (driver) {
            await driver.quit();
        }
    });

    it('TC_WEB_001: Standard Valid User Login Flow', async function () {
        await driver.get(`${BASE_URL}/login`);
        const emailInput = await driver.findElement(By.id('email'));
        const passwordInput = await driver.findElement(By.id('password'));
        const loginBtn = await driver.findElement(By.id('login-btn'));

        await emailInput.sendKeys('trainer.admin@example.com');
        await passwordInput.sendKeys('SecurePass123!');
        await loginBtn.click();

        await driver.wait(until.urlContains('/dashboard'), 10000);
        const currentUrl = await driver.getCurrentUrl();
        assert.ok(currentUrl.includes('/dashboard'), 'User was not redirected to dashboard after valid login');
    });

    it('TC_WEB_002: Invalid Password Rejection', async function () {
        await driver.get(`${BASE_URL}/login`);
        await driver.findElement(By.id('email')).sendKeys('trainer.admin@example.com');
        await driver.findElement(By.id('password')).sendKeys('WrongPassword!');
        await driver.findElement(By.id('login-btn')).click();

        const errorMsg = await driver.wait(until.elementLocated(By.className('error-banner')), 5000);
        const text = await errorMsg.getText();
        assert.ok(text.includes('Invalid credentials'), 'Error message was not displayed');
    });

    it('TC_WEB_003: Empty Email & Password Field Validation', async function () {
        await driver.get(`${BASE_URL}/login`);
        await driver.findElement(By.id('login-btn')).click();

        const emailError = await driver.findElement(By.id('email-error'));
        const text = await emailError.getText();
        assert.ok(text.includes('Required'), 'Validation error not displayed for empty fields');
    });

    it('TC_WEB_004: Password Masking Toggle Interaction', async function () {
        await driver.get(`${BASE_URL}/login`);
        const passwordInput = await driver.findElement(By.id('password'));
        const toggleBtn = await driver.findElement(By.id('toggle-password-visibility'));

        let inputType = await passwordInput.getAttribute('type');
        assert.strictEqual(inputType, 'password');

        await toggleBtn.click();
        inputType = await passwordInput.getAttribute('type');
        assert.strictEqual(inputType, 'text');
    });

    it('TC_WEB_005: Session Logout & Token Invalidation', async function () {
        await driver.get(`${BASE_URL}/login`);
        await driver.findElement(By.id('email')).sendKeys('trainer.admin@example.com');
        await driver.findElement(By.id('password')).sendKeys('SecurePass123!');
        await driver.findElement(By.id('login-btn')).click();
        await driver.wait(until.urlContains('/dashboard'), 10000);

        const logoutBtn = await driver.findElement(By.id('logout-btn'));
        await logoutBtn.click();

        await driver.wait(until.urlContains('/login'), 5000);
        const currentUrl = await driver.getCurrentUrl();
        assert.ok(currentUrl.includes('/login'), 'User not redirected to login page after logout');
    });
});
