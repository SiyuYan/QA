module.exports = {
  'Successfully Sign in': function(browser) {
  var login = browser.page.simpleLogin();

    login.navigate()
      .waitForElementVisible( '@signInIcon', 5000 )
      .click('@signInIcon')
      .verify.visible('@userEmail')
      .verify.visible('@passWord')
      .setValue('@userEmail', 'nightwatch@test.com')
      .setValue('@passWord', 'Password1')
      .click('@signIn')
      .verify.containsText('@userName', 'Hi, nightwatch')

      browser.end()
  }
}