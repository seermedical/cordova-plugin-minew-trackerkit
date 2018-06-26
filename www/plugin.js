
var exec = require('cordova/exec');

var PLUGIN_NAME = 'MinewTrackerkit';

var MinewTrackerkit = {
  find: function(id, success, failure) {
    exec(success, failure, PLUGIN_NAME, 'find', [id]);
  },
  startScan: function(success, failure) {
    exec(success, failure, PLUGIN_NAME, 'startScan', []);
  },
  stopScan: function(success, failure) {
    exec(success, failure, PLUGIN_NAME, 'stopScan', []);
  },
  connect: function(id, success, failure) {
  exec(success, failure, PLUGIN_NAME, 'connect', [id]);
  },
  disconnect: function(id, success, failure) {
  exec(success, failure, PLUGIN_NAME, 'disconnect', [id]);
  },
  subscribeToClick: function(id, success, failure) {
  exec(success, failure, PLUGIN_NAME, 'subscribeToClick', [id]);
  },
  subscribeToStatus: function(id, success, failure) {
  exec(success, failure, PLUGIN_NAME, 'subscribeToStatus', [id]);
  }
};

module.exports = MinewTrackerkit;
