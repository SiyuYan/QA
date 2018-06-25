module.exports = function () {
    var Cucumber = require('cucumber'),
        formatter = Cucumber.Listener.JsonFormatter(),
        path = require('path'),
        fs = require('fs');
    formatter.log = function (json) {
        fs.writeFile('./reports/e2e.json', json, function (err) {
            if (err) throw err;
            console.log('');
        });
    };
    this.registerListener(formatter);
};
