
exports.config = {
    seleniumAddress: 'http://127.0.0.1:4444/wd/hub',
    framework: 'cucumber',
    specs: [
        'features/*.feature'
    ],
    capabilities: {
        browserName: 'chrome'
    }

};