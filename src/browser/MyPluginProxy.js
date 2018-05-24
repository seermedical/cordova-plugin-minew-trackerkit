module.exports = {
    ping: function (success, error) {
        setTimeout(function () {
            success('pong');
        }, 0);
    }
};

require("cordova/exec/proxy").add("MyPlugin", module.exports);
