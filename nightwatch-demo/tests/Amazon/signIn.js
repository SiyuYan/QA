module.exports = {
  'Successfully Sign in': function(browser) {
    browser
      .init()
      .waitForElementVisible( '#nav-link-accountList', 5000 )
      .click('#nav-link-accountList')
      .verify.visible('#ap_email')
      .verify.visible('#ap_password')
      .setValue('#ap_email', 'nightwatch@test.com')
      .setValue('#ap_password', 'Password1')
      .click('#signInSubmit')
      .verify.containsText('#hud-customer-name > div > a', 'Hi, nightwatch')
      .end()
  },

  'Sign in failed': function(browser) {
      browser
        .init()
        .waitForElementVisible( '#nav-link-accountList', 5000 )
        .click('#nav-link-accountList')
        .verify.visible('#ap_email')
        .verify.visible('#ap_password')
        .setValue('#ap_email', 'nightwatch@test.com')
        .setValue('#ap_password', 'Password')
        .click('#signInSubmit')
        .verify.containsText('#hud-customer-name > div > a', 'Hi, nightwatch')
        .end()
    },
}