module.exports = {
  'Demo test Google' : function (client) {
    client
      .url('http://www.google.com')
      .waitForElementVisible('body', 2000)
      .assert.title('Google')
      .assert.visible('input[type=text]')
      .setValue('input[type=text]', 'ThoughtWorks')
      .click('#hplogo')
      .click('input[type="submit"]:nth-child(1)')
      .pause(1000)
      .assert.containsText('#rso > div:nth-child(1)',
        'ThoughtWorks')
      .end();
  }
}