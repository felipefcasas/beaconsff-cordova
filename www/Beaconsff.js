var exec = require('cordova/exec');

module.exports = {
	startMonitoring: function(params, success, error) {
		exec(success, error, 'Beaconsff', 'startMonitoring', [params]);
	},

	stopMonitoring: function(success, error) {
		exec(success, error, 'Beaconsff', 'stopMonitoring', []);
	},

	startAdvertisiment: function(params, success, error) {
		exec(success, error, 'Beaconsff', 'startAdvertisiment', [params]);
	},

	stopAdvertisiment: function(params, success, error) {
		exec(success, error, 'Beaconsff', 'stopAdvertisiment', [params]);
	},

	requestPermissions: function(success, error) {
		exec(success, error, 'Beaconsff', 'requestPermissions', []);
	}
};