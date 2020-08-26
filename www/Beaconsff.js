var exec = require('cordova/exec');

module.exports = {
	startMonitoring: function (params, success, error) {
		exec(success, error, 'Beaconsff', 'startMonitoring', []);
	},

	stopMonitoring: function (success, error) {
		exec(success, error, 'Beaconsff', 'stopMonitoring', []);
	},

	startAdvertising: function (params, success, error) {
		exec(success, error, 'Beaconsff', 'startAdvertising', [params]);
	},

	stopAdvertising: function (params, success, error) {
		exec(success, error, 'Beaconsff', 'stopAdvertising', []);
	},

	requestPermissions: function (success, error) {
		exec(success, error, 'Beaconsff', 'requestPermissions', []);
	},

	getServiceStatus: function (success, error) {
		exec(success, error, 'Beaconsff', 'getServiceStatus', []);
	}
};