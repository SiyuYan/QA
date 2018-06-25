var TakeScreenshot = function () {

    this.After(function (scenario, done) {
        if (scenario.isFailed()) {
            browser.takeScreenshot().then(function (png) {
                var decodedImage = new Buffer(png, 'base64').toString('binary');
                scenario.attach(decodedImage, 'image/png');
                done();
            });
        } else {
            done();
        }
    });
};
module.exports=TakeScreenshot;