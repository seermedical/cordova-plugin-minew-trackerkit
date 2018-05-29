/**
 */
package com.minew;

import android.Manifest;
import android.util.Log;
import android.content.Context;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import com.minewtech.mttrackit.MTTracker;
import com.minewtech.mttrackit.MTTrackerManager;
import com.minewtech.mttrackit.enums.BluetoothState;
import com.minewtech.mttrackit.enums.ConnectionState;
import com.minewtech.mttrackit.enums.TrackerModel;
import com.minewtech.mttrackit.interfaces.ConnectionStateCallback;
import com.minewtech.mttrackit.interfaces.OperationCallback;
import com.minewtech.mttrackit.interfaces.ScanTrackerCallback;
import com.minewtech.mttrackit.interfaces.TrackerManagerListener;

import static com.minewtech.mttrackit.enums.ConnectionState.DeviceLinkStatus_Disconnect;

// import java.util.Date;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MinewTrackerkit extends CordovaPlugin {

  private static final String TAG = "MinewTrackerkit";
  private static Context mContext;
  private CallbackContext scanCallback;

  // Android 23 requires new permissions for BluetoothLeScanner.startScan()
  private static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
  private static final int REQUEST_ACCESS_COARSE_LOCATION = 2;
  private static final int PERMISSION_DENIED_ERROR = 20;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    Log.d(TAG, "Initializing MinewTrackerkit");
    mContext = this.cordova.getActivity().getApplicationContext();
    getRequiredPermissions();
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if (action.equals("bleStatus")) {
      this.bleStatus(callbackContext);
      return true;
    } else if (action.equals("startScan")) {
      this.startScan(callbackContext);
      return true;
    } else if (action.equals("stopScan")) {
      this.stopScan(callbackContext);
      return true;
    } else if (action.equals("bind")) {
      String macAddress = args.getString(0);
      this.bind(callbackContext, macAddress);
      return true;
    }
    return false;
  }

  private void bleStatus(CallbackContext callbackContext) {
    BluetoothState bluetoothState = MTTrackerManager.getInstance(mContext).checkBluetoothState();
    Log.d(TAG, "status: " + bluetoothState);
    switch (bluetoothState) {
        case BluetoothStateNotSupported:
//          final PluginResult result = new PluginResult(PluginResult.Status.OK, status);
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "not supported"));
        case BluetoothStatePowerOff:
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, 0));
        case BluetoothStatePowerOn:
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, 1));
    }

  }

  private void startScan(CallbackContext callbackContext) {
    Log.d(TAG, "start scan");
    scanCallback = callbackContext;
    MTTrackerManager.getInstance(mContext).startScan(this.scanTrackerCallback);
  }

  private void stopScan(CallbackContext callbackContext) {
    Log.d(TAG, "stop scan");
    MTTrackerManager.getInstance(mContext).stopScan();
  }

  private ScanTrackerCallback scanTrackerCallback = new ScanTrackerCallback() {
    @Override
    public void onScannedTracker(LinkedList<MTTracker> trackers) {
        for (MTTracker tracker : trackers) {
          if (scanCallback != null) {
            String mac = tracker.getMacAddress();
            PluginResult result = new PluginResult(PluginResult.Status.OK, mac);
            result.setKeepCallback(true);
            scanCallback.sendPluginResult(result);
          }
        }
    }
  };

  private void bind(CallbackContext callbackContext, String macAddress) {
    Log.d(TAG, "binding to: " + macAddress);
  }

  private void getRequiredPermissions() {
    if(!PermissionHelper.hasPermission(this, ACCESS_COARSE_LOCATION)) {
      PermissionHelper.requestPermission(this, REQUEST_ACCESS_COARSE_LOCATION, ACCESS_COARSE_LOCATION);
      return;
    }
  }


}
