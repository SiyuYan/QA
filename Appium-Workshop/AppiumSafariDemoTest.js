"use strict";

var wd = require("wd");
var chai = require("chai");
var chaiAsPromised = require("chai-as-promised");

chai.use(chaiAsPromised);
chai.should();
chaiAsPromised.transferPromiseness = wd.transferPromiseness;

var desired = {
	"appium-version": "1.5",
	platformName: "iOS",
	platformVersion: "8.3",
	deviceName: "iPhone 6 Plus",
	browserName: "Safari",
	app: "",
};

var asserters = wd.asserters;
var browser = wd.promiseChainRemote("0.0.0.0", 4723);

browser
.init(desired)
.get("http://www.baidu.com")
.waitForElementByCss("#index-kw" , 20000)
.elementById("index-kw").sendKeys("徐大爷")
.elementByCss("#index-bn").click()
.waitForElementByCss("#results", asserters.isDisplayed,20000)
.done();

