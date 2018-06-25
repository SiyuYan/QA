
module.exports = {
  'Login Successfully': function(browser) {
    var login = browser.page.commandsLogin();

    login.navigate()
      .goToSignInPage()
      .fillInAccountInfo()
      .submit()
      .validateError()

    browser.end();
  }
}