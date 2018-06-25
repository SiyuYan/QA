var loginCommands = {
	goToSignInPage: function() {
		return this.waitForElementVisible( '@signInIcon', 5000 )
			.click('@signInIcon')
            .verify.visible('@userEmail')
            .verify.visible('@passWord')
	},
	fillInAccountInfo: function(username, password) {
		return this
			.setValue('@userEmail', 'nightwatch@test.com')
            .setValue('@passWord', 'Password1')
	},
	submit: function() {
		return this
			.click('@signIn')
	},
	validateError: function(errorMessage) {
		return this.verify.containsText('@userName', 'Hi, nightwatch')
	}
};


module.exports = {
	commands: [loginCommands],
	url: function() { 
		return this.api.launchUrl; 
	},
	elements: {
    		signInIcon: {
    			selector: '#nav-link-accountList'
    		},
    		userEmail: {
    			selector: '#ap_email'
    		},
    		passWord: {
    			selector: '#ap_password'
    		},
    		signIn: {
    			selector: '#signInSubmit'
    		},
    		userName: {
            	selector: '#hud-customer-name > div > a'
            }
    }
};