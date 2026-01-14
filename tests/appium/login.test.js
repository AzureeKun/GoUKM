const { remote } = require('webdriverio');
const assert = require('chai').assert;

const capabilities = {
    platformName: 'Android',
    'appium:automationName': 'UiAutomator2',
    'appium:deviceName': 'Android Emulator',
    'appium:app': 'C:\\TestApp\\app-debug.apk',
    'appium:appPackage': 'com.example.goukm',
    'appium:appActivity': 'com.example.goukm.MainActivity',
    'appium:ensureWebviewsHavePages': true,
    'appium:nativeWebScreenshot': true,
    'appium:newCommandTimeout': 3600,
    'appium:connectHardwareKeyboard': true
};

const wdOpts = {
    hostname: process.env.APPIUM_HOST || 'localhost',
    port: parseInt(process.env.APPIUM_PORT, 10) || 4723,
    logLevel: 'info',
    capabilities,
};

describe('Login Flow Test', function () {
    this.timeout(60000);
    let driver;

    before(async function () {
        driver = await remote(wdOpts);
    });

    after(async function () {
        if (driver) {
            await driver.deleteSession();
        }
    });

    it('should login with valid credentials', async function () {
        const el1 = await driver.$('android=new UiSelector().className("android.widget.EditText").instance(0)');
        await el1.waitForDisplayed({ timeout: 10000 });
        await el1.setValue("A203535");

        const el2 = await driver.$('android=new UiSelector().className("android.widget.EditText").instance(1)');
        await el2.setValue("UKMA203535");

        const el3 = await driver.$('android=new UiSelector().className("android.widget.Button").instance(1)');
        await el3.click();
    });
});
