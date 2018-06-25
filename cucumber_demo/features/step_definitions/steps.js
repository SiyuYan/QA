var chai = require('chai');
var chaiAsPromised = require('chai-as-promised');
chai.use(chaiAsPromised);
var expect = chai.expect;

module.exports = function () {

    this.Given(/^I go on "([^"]*)"$/, function (arg1, done) {
        browser.driver.get('http://baidu.com');
        done()
    });

    this.Then(/^the title should equal "([^"]*)"$/, function (arg1, done) {
        expect(browser.driver.getTitle()).to.eventually.equal('百度一下，你就知道').and.notify(done);
    });

    // this.Then(/^should return baidu$/, function (done) {
    //    // browser.driver.findElement(by.id('firstName'));
    //     element(by.id('firstName')).sendKeys('Rainie');
    //     expect(browser.driver.getTitle()).to.eventually.equal('百度一下，你就知道').and.notify(done);
    // });

};