
var exec = require('cordova/exec');

var PLUGIN_NAME = 'MinewTrackerkit';

var MinewTrackerkit = {
  testFunction: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'testFunction', []);
  }
};

module.exports = MinewTrackerkit;
