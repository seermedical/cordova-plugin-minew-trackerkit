
var exec = require('cordova/exec');

var PLUGIN_NAME = 'MinewTrackerkit';

var MinewTrackerkit = {
  bleStatus: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'bleStatus', []);
  },
  startScan: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'startScan', []);
  },
  bind: function(id, cb) {
  exec(cb, null, PLUGIN_NAME, 'bind', [id]);
  }
};

module.exports = MinewTrackerkit;
