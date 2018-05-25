/**
 */
package com.minew;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;
// import java.util.Date;

public class MinewTrackerkit extends CordovaPlugin {
  private static final String TAG = "MinewTrackerkit";

  // // Android 23 requires new permissions for BluetoothLeScanner.startScan()
  // private static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
  // private static final int REQUEST_ACCESS_COARSE_LOCATION = 2;
  // private static final int PERMISSION_DENIED_ERROR = 20;
  // private CallbackContext permissionCallback;
  // private int scanSeconds;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    Log.d(TAG, "Initializing MinewTrackerkit");
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if(action.equals("bleStatus")) {
      // get sharedinstance of Manager
      MTTrackerManager manager = MTTrackerManager.getInstance(context);
      String status = manager.checkBluetoothState;
      Log.d(TAG, status);
      final PluginResult result = new PluginResult(PluginResult.Status.OK, status);
      callbackContext.sendPluginResult(result);
    }
    return true;
  }


}
