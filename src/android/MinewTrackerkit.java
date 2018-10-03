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
import com.minewtech.mttrackit.TrackerException;

import com.minewtech.mttrackit.interfaces.ConnectionStateCallback;
import com.minewtech.mttrackit.interfaces.OperationCallback;
import com.minewtech.mttrackit.interfaces.ScanTrackerCallback;
import com.minewtech.mttrackit.interfaces.MTTrackerListener;
import com.minewtech.mttrackit.interfaces.TrackerManagerListener;
import com.minewtech.mttrackit.interfaces.ReceiveListener;

import com.minewtech.mttrackit.enums.TrackerModel;
import static com.minewtech.mttrackit.enums.TrackerModel.*;
import com.minewtech.mttrackit.enums.BluetoothState;
import com.minewtech.mttrackit.enums.ConnectionState;
import static com.minewtech.mttrackit.enums.ConnectionState.*;
import com.minewtech.mttrackit.enums.ReceiveIndex;
import static com.minewtech.mttrackit.enums.ReceiveIndex.*;

import java.util.*;

public class MinewTrackerkit extends CordovaPlugin {

  private static final String TAG = "MinewTrackerkit";
  private static Context mContext;

  // this is where the cordova callbacks get put so that the MT blocks can use them
  private CallbackContext scanCallback;
  private CallbackContext findCallback;
  private CallbackContext connectCallback;
  private CallbackContext clickCallback;
  private CallbackContext disconnectCallback;
  private CallbackContext unbindCallback;

  // central tracker manager and list of buttons
  private static Map<String, MTTracker> peripherals;
  private static MTTrackerManager manager;
  private static MTTracker myTracker;

  // Android 23 requires new permissions for BluetoothLeScanner.startScan()
  private static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
  private static final int REQUEST_ACCESS_COARSE_LOCATION = 2;
  private static final int PERMISSION_DENIED_ERROR = 20;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    Log.d(TAG, "Initializing MinewTrackerkit");
    mContext = this.cordova.getActivity().getApplicationContext();
    peripherals = new LinkedHashMap<String, MTTracker>();
    manager = MTTrackerManager.getInstance(mContext);
    manager.setPassword("B3agle!!");
    if(!PermissionHelper.hasPermission(this, ACCESS_COARSE_LOCATION)) {
      getRequiredPermissions();
    }
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if (action.equals("find")) {
      String macAddress = args.getString(0);
      this.find(callbackContext, macAddress);
      return true;
    } else if (action.equals("startScan")) {
      this.startScan(callbackContext);
      return true;
    } else if (action.equals("stopScan")) {
      this.stopScan(callbackContext);
      return true;
    } else if (action.equals("connect")) {
      String macAddress = args.getString(0);
      this.connect(callbackContext, macAddress);
      return true;
    } else if (action.equals("disconnect")) {
      String macAddress = args.getString(0);
      this.disconnect(callbackContext, macAddress);
      return true;
    } else if (action.equals("subscribeToClick")) {
      String macAddress = args.getString(0);
      this.subscribeToClick(callbackContext, macAddress);
      return true;
    } else if (action.equals("subscribeToStatus")) {
      String macAddress = args.getString(0);
      this.subscribeToStatus(callbackContext, macAddress);
      return true;
    }
    return false;
  }

  private void startScan(CallbackContext callbackContext) {
    Log.d(TAG, "start scan");
    if(PermissionHelper.hasPermission(this, ACCESS_COARSE_LOCATION)) {
      scanCallback = callbackContext;
      manager.startScan(this.scanTrackerCallback);
    } else {
      Log.d(TAG, "NO PERMISSION");
      PluginResult result = new PluginResult(PluginResult.Status.ERROR);
      scanCallback.sendPluginResult(result);
    }
  }

  private void stopScan(CallbackContext callbackContext) {
    Log.d(TAG, "stop scan");
    peripherals = new LinkedHashMap<String, MTTracker>();
    scanCallback = null;
    manager.stopScan();
    PluginResult result = new PluginResult(PluginResult.Status.OK);
    callbackContext.sendPluginResult(result);
  }

  private void find(CallbackContext callbackContext, String macAddress) {
    // connectCallback = null;
    Log.d(TAG, "find: " + macAddress);
    findCallback = callbackContext;
    myTracker = manager.bindMTTracker(macAddress);
    manager.bindingVerify(myTracker, new ConnectionStateCallback() {
      @Override
      public void onUpdateConnectionState(final boolean success, final TrackerException trackerException) {
      }
    });
    manager.setTrackerManangerListener(new TrackerManagerListener() {
      PluginResult result;
      @Override
      public void onUpdateBindTrackers(ArrayList<MTTracker> mtTrackers) {

      }

      @Override
      public void onUpdateConnectionState(MTTracker tracker, ConnectionState status) {
        switch (status) {
          case DeviceLinkStatus_Connected:
            Log.d(TAG,"Connected");
            result = new PluginResult(PluginResult.Status.OK, asJSONObject(tracker));
            findCallback.sendPluginResult(result);
            return;
          default:
            Log.d(TAG,"Disconnected");
            result = new PluginResult(PluginResult.Status.ERROR);
            findCallback.sendPluginResult(result);
            return;
        }
      }
    });
  }

  private void connect(CallbackContext callbackContext, String macAddress) {
    Log.d(TAG, "connect to: " + macAddress);
    connectCallback = callbackContext;
    if (peripherals.containsKey(macAddress)) {
      MTTracker trackerToBind = peripherals.get(macAddress);
      myTracker = trackerToBind;
      manager.bindingVerify(trackerToBind, this.connectionCallback);
    } else {
      PluginResult result = new PluginResult(PluginResult.Status.ERROR);
      connectCallback.sendPluginResult(result);
    }
  }

  private void disconnect(CallbackContext callbackContext, String macAddress) {
    Log.d(TAG, "disconnect: " + macAddress);
    unbindCallback = callbackContext;
    if (myTracker.getMacAddress().equals(macAddress)) {
      manager.unBindMTTracker(macAddress, new OperationCallback() {
        @Override
        public void onOperation(boolean success, TrackerException mtException) {
          if (success) {
            myTracker = null;
            peripherals = new LinkedHashMap<String, MTTracker>();
            manager = MTTrackerManager.getInstance(mContext);
            findCallback = null;
            connectCallback = null;
            disconnectCallback = null;
            clickCallback = null;
            PluginResult result = new PluginResult(PluginResult.Status.OK);
            unbindCallback.sendPluginResult(result);
          } else {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR);
            unbindCallback.sendPluginResult(result);
          }
        }
      });
    } else {
      PluginResult result = new PluginResult(PluginResult.Status.ERROR);
      unbindCallback.sendPluginResult(result);
    }
  }

  private void subscribeToClick(CallbackContext callbackContext, String macAddress) {
    Log.d(TAG, "subscribe to: " + macAddress);
    clickCallback = callbackContext;
    if (myTracker.getMacAddress().equals(macAddress)) {
      myTracker.setReceiveListener(clickListener);
    } else {
      // try connect?
      PluginResult result = new PluginResult(PluginResult.Status.ERROR);
      scanCallback.sendPluginResult(result);
    }
  }

  private void subscribeToStatus(CallbackContext callbackContext, String macAddress) {
    if (myTracker.getMacAddress().equals(macAddress)) {
      Log.d(TAG, "subscribe to status: " + macAddress);
      disconnectCallback = callbackContext;
      myTracker.setTrackerListener(connectionListener);
    } else {
      // do something
      PluginResult result = new PluginResult(PluginResult.Status.ERROR);
      scanCallback.sendPluginResult(result);
    }
  }

  private MTTrackerListener connectionListener = new MTTrackerListener() {
    @Override
    public void onUpdateTracker(MTTracker mtTracker) {
    }

    @Override
    public void onUpdateConnectionState(ConnectionState connectionState) {
      PluginResult result;
      switch (connectionState) {
        case DeviceLinkStatus_Connected:
          Log.d(TAG, "connected");
          if (disconnectCallback != null) {
            result = new PluginResult(PluginResult.Status.OK);
            result.setKeepCallback(true);
            disconnectCallback.sendPluginResult(result);
          }
          break;
        case DeviceLinkStatus_Disconnect:
          Log.d(TAG, "disconnected");
          if (disconnectCallback != null) {
            result = new PluginResult(PluginResult.Status.ERROR);
            result.setKeepCallback(true);
            disconnectCallback.sendPluginResult(result);
          }
          break;
        default:
          Log.d(TAG, "connection change");
          break;
      }

    }
  };

  private ReceiveListener clickListener = new ReceiveListener() {
    @Override
    public void onReceive(ReceiveIndex index) {
      PluginResult result;
      switch (index) {
        case InstrucIndex_ButtonPushed:
          if (clickCallback != null) {
            result = new PluginResult(PluginResult.Status.OK);
            result.setKeepCallback(true);
            clickCallback.sendPluginResult(result);
          }
      }
    }
  };

  private ScanTrackerCallback scanTrackerCallback = new ScanTrackerCallback() {
    @Override
    public void onScannedTracker(LinkedList<MTTracker> trackers) {
      PluginResult result;
      for (MTTracker tracker : trackers) {
        if (scanCallback != null) {
          String mac = tracker.getMacAddress();
          if (!peripherals.containsKey(mac)) {
            peripherals.put(mac, tracker);
            result = new PluginResult(PluginResult.Status.OK, asJSONObject(tracker));
            result.setKeepCallback(true);
            scanCallback.sendPluginResult(result);
          }
        }
      }
    }
  };

  private ConnectionStateCallback connectionCallback = new ConnectionStateCallback() {
    @Override
    public void onUpdateConnectionState(final boolean success, final TrackerException trackerException) {
      if (connectCallback != null) {
        PluginResult result;
        // result.setKeepCallback(true); ????
        if (success) {
          myTracker = manager.bindMTTracker(myTracker.getMacAddress());
          result = new PluginResult(PluginResult.Status.OK);
          connectCallback.sendPluginResult(result);
          return;
        } else {
          result = new PluginResult(PluginResult.Status.ERROR);
          connectCallback.sendPluginResult(result);
          return;
        }
      }
    }
  };

  private JSONObject asJSONObject(MTTracker tracker) {
    JSONObject json = new JSONObject();
    try {
      json.put("address", tracker.getMacAddress());
      json.put("rssi", tracker.getRssi());
      json.put("battery", tracker.getBattery());

      TrackerModel model = tracker.getName();
      switch (model) {
        case MODEL_F4S:
          json.put("model", "F4S");
          break;
        case MODEL_Finder:
          json.put("model", "Finder");
          break;
        default:
          json.put("model", null);
          break;
      }

      ConnectionState status = tracker.getConnectionState();
      switch (status) {
        case DeviceLinkStatus_Connected:
          json.put("status", "connected");
          break;
        default:
          json.put("status", "disconnected");
          break;
      }

    } catch (JSONException e) { // this shouldn't happen
      e.printStackTrace();
    }
    return json;
  }

  private void getRequiredPermissions() {
    PermissionHelper.requestPermission(this, REQUEST_ACCESS_COARSE_LOCATION, ACCESS_COARSE_LOCATION);
    return;
  }

}
