package me.rost.myplugin;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.provider.Settings;

public class MyPlugin extends CordovaPlugin {
    public static final String TAG = "MyPlugin";

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("ping")) {
            callbackContext.success("pong");
        } else {
            return false;
        }
        return true;
    }
}
