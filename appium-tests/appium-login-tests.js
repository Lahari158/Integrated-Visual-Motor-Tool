const { remote } = require('webdriverio');
const assert = require('assert');

describe('Appium Mobile E2E Test Suite - Visual Monitor Trainer', function () {
    this.timeout(120000);
    let driver;

    const opts = {
        path: '/',
        port: 4723,
        capabilities: {
            platformName: 'Android',
            'appium:automationName': 'UiAutomator2',
            'appium:deviceName': 'Android Emulator',
            'appium:appPackage': 'com.pdd.app',
            'appium:appActivity': 'com.pdd.app.SplashActivity',
            'appium:noReset': false
        }
    };

    beforeEach(async function () {
        driver = await remote(opts);
    });

    afterEach(async function () {
        if (driver) {
            await driver.deleteSession();
        }
    });

    it('TC_APP_001: Launch Splash Screen & Auto Transition to Main', async function () {
        const splashLogo = await driver.$('~App Splash Logo');
        assert.ok(await splashLogo.isDisplayed(), 'Splash screen logo is not rendered');

        await driver.pause(3000); // Wait for transition
        const mainContainer = await driver.$('android=new UiSelector().className("android.view.View")');
        assert.ok(await mainContainer.isDisplayed(), 'Failed to transition from splash to main layout');
    });

    it('TC_APP_002: Mobile Touch Interaction & Jetpack Compose Navigation', async function () {
        await driver.pause(2000);
        const loginTab = await driver.$('android=new UiSelector().text("Login")');
        if (await loginTab.isExisting()) {
            await loginTab.click();
            const emailField = await driver.$('android=new UiSelector().className("android.widget.EditText").instance(0)');
            await emailField.setValue('mobile.user@example.com');
            const enteredText = await emailField.getText();
            assert.strictEqual(enteredText, 'mobile.user@example.com');
        }
    });
});
