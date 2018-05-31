
var exec = require('cordova/exec');

var PLUGIN_NAME = 'MinewTrackerkit';

var MinewTrackerkit = {
  bleStatus: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'bleStatus', []);
  },
  startScan: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'startScan', []);
  },
  stopScan: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'stopScan', []);
  },
  connect: function(id, cb) {
  exec(cb, null, PLUGIN_NAME, 'connect', [id]);
  }
};

module.exports = MinewTrackerkit;
