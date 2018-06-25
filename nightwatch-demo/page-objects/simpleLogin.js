module.exports = {
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
}