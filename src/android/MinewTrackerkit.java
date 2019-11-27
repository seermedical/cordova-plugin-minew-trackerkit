/**
 */
package com.minew;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

import java.text.SimpleDateFormat;
import java.util.*;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import android.content.Intent;
import android.content.Context;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.IBinder;
import android.os.Bundle;
import android.annotation.TargetApi;

import static com.minewtech.mttrackit.enums.BluetoothState.BluetoothStatePowerOn;

public class MinewTrackerkit extends CordovaPlugin {
    private static final String TAG = "MinewTrackerkit";
    private static final int PERMISSION_COARSE_LOCATION = 2;
    private static final int PERMISSION_FINE_LOCATION = 2;

    private static Context mContext;

    // this is where the cordova callbacks get put so that the MT blocks can use them
    private CallbackContext scanCallback;
    private CallbackContext findCallback;
    private CallbackContext connectCallback;
    private CallbackContext clickCallback;
    private CallbackContext disconnectCallback; // the subscription callback
    private CallbackContext unbindCallback;

    // central tracker manager and list of buttons
    private static Map<String, MTTracker> peripherals;
    private static MTTrackerManager manager;
    private static MTTracker myTracker;

    // Android 23 requires new permissions for BluetoothLeScanner.startScan()
    private static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 2;
    private static final int PERMISSION_DENIED_ERROR = 20;

    private static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 3;

    private static boolean backgroundStatus;

    private DbWorkerThread mDbWorkerThread;
    private SeerDatabase mDb;
    private Boolean foregroundServiceRunning;
    private String userId;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {

        super.initialize(cordova, webView);

        mDbWorkerThread = new DbWorkerThread("dbWorkerThread");
        mDbWorkerThread.start();
        backgroundStatus = false;
        mContext = this.cordova.getActivity().getApplicationContext();

        mDb = SeerDatabase.getInstance(mContext);

        manager = MTTrackerManager.getInstance(mContext);
        manager.setPassword("B3agle!!");

        foregroundServiceRunning = false;

        if(!PermissionHelper.hasPermission(this, ACCESS_COARSE_LOCATION)) {
            getRequiredPermissions(REQUEST_ACCESS_COARSE_LOCATION, ACCESS_COARSE_LOCATION);
        }

        if(!PermissionHelper.hasPermission(this, ACCESS_FINE_LOCATION)) {
            getRequiredPermissions(REQUEST_ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION);
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
        } else if (action.equals("updateBackgroundStatus")) {
            Log.d(TAG, "SettingBackgroundStatus");
            setBackgroundStatus(callbackContext, args.getBoolean(0));
            return true;
        } else if (action.equals("setUserId")) {
            Log.d(TAG, "SettingUserId");
            this.setUserId(callbackContext, args.getString(0));
            return true;
        } else if (action.equals("fetchButtonData")) {
            Log.d(TAG, "Fetching Button Data");
            this.fetchButtonData(callbackContext);
            return true;
        } else if (action.equals("deleteButtonData")) {
            Log.d(TAG, "Deleting Button Data");
            this.deleteButtonData(callbackContext);
            return true;
        }
        return false;
    }

    private void startScan(CallbackContext callbackContext) {
        peripherals = new LinkedHashMap<String, MTTracker>();
        if(PermissionHelper.hasPermission(this, ACCESS_COARSE_LOCATION) || PermissionHelper.hasPermission(this, ACCESS_FINE_LOCATION)) {
            scanCallback = callbackContext;
            if(manager.checkBluetoothState() == BluetoothStatePowerOn) {
                manager.startScan(this.scanTrackerCallback);
            }
        } else {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR);
            scanCallback.sendPluginResult(result);
        }
    }

    private void stopScan(CallbackContext callbackContext) {
        scanCallback = null;
        manager.stopScan();
        PluginResult result = new PluginResult(PluginResult.Status.OK);
        callbackContext.sendPluginResult(result);
    }

    private void find(CallbackContext callbackContext, String macAddress) {
        connectCallback = callbackContext;
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
                    myTracker = null;
                    findCallback = null;
                    connectCallback = null;
                    disconnectCallback = null;
                    clickCallback = null;
                    PluginResult result = new PluginResult(PluginResult.Status.OK);
                    unbindCallback.sendPluginResult(result);
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
            startForegroundService();
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
                    if(backgroundStatus == false) {
                        if (clickCallback != null) {
                            result = new PluginResult(PluginResult.Status.OK);
                            result.setKeepCallback(true);
                            clickCallback.sendPluginResult(result);
                        }
                    }
                    else {
                        TimeZone timezone = TimeZone.getDefault();
                        int seconds = timezone.getOffset(Calendar.ZONE_OFFSET)/1000;
                        double minutes = seconds/60;
                        double hours = minutes/60;

                        ButtonData buttonData = new ButtonData();
                        buttonData.userId = userId;
                        buttonData.start_time = System.currentTimeMillis();
                        buttonData.created_at = System.currentTimeMillis();
                        buttonData.timezone = (float) hours;
                        insertButtonData(buttonData);

                        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        int icon = mContext.getResources().getIdentifier((String) "star", "drawable", mContext.getPackageName());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            notificationManager.deleteNotificationChannel("button.service.channel");

                            NotificationChannel channel = new NotificationChannel("button.service.channel", "Button Services", NotificationManager.IMPORTANCE_DEFAULT);
                            channel.setDescription("Enables button processing.");
                            mContext.getSystemService(NotificationManager.class).createNotificationChannel(channel);

                            Notification notification = new Notification.Builder(mContext, "button.service.channel")
                                    .setContentTitle("Seer")
                                    .setContentText("Your event has been recorded. Open the Seer app to add more details.")
                                    .setOngoing(true)
                                    .setSmallIcon(icon == 0 ? 17301514 : icon) // Default is the star icon
                                    .build();

                            notificationManager.notify(91, notification);
                        } else {
                            Notification notification = new Notification.Builder(mContext)
                                    .setContentTitle("Seer")
                                    .setContentText("Your event has been recorded. Open the Seer app to add more details.")
                                    .setOngoing(true)
                                    .setSmallIcon(icon == 0 ? 17301514 : icon) // Default is the star icon
                                    .build();

                            notificationManager.notify(91, notification);
                        }

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
                // TODO: investigate holding open this callback result.setKeepCallback(true)
                if (success) {

                    startForegroundService();

                    myTracker = manager.bindMTTracker(myTracker.getMacAddress());
                    result = new PluginResult(PluginResult.Status.OK);
                    connectCallback.sendPluginResult(result);
                    return;
                } else {
                    Log.d("Connect", "FAIL");
                    Log.d("Connect", String.valueOf(trackerException));

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

    private void getRequiredPermissions(int permissionId, String permissionText) {
        PermissionHelper.requestPermission(this, permissionId, permissionText);
        return;
    }

    private void insertButtonData(ButtonData buttonData) {

        final ButtonData insertButtonData = buttonData;
        Runnable task = new Runnable() {
            public void run() {
                mDb.buttonDataDao().insert(insertButtonData);
                List<ButtonData> allButtonData = mDb.buttonDataDao().fetch(userId);
            }};
        mDbWorkerThread.postTask(task);
    }

    private void fetchButtonData(CallbackContext callbackContext) {
        Runnable task = new Runnable() {

            public void run () {
                JSONArray buttonDataConverted = new JSONArray();
                List<ButtonData> allButtonData = mDb.buttonDataDao().fetch(userId);

                Gson gson = new Gson();

                String listString = gson.toJson(
                                    allButtonData,
                                    new TypeToken<ArrayList<ButtonData>>() {}.getType());

                try {
                    buttonDataConverted =  new JSONArray(listString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, buttonDataConverted);
                callbackContext.sendPluginResult(result);
            }
        };

        mDbWorkerThread.postTask(task);
    }

    private void deleteButtonData(CallbackContext callbackContext) {
        Runnable task = new Runnable() {
            public void run() {
                mDb.buttonDataDao().delete(userId);
                PluginResult result = new PluginResult(PluginResult.Status.OK);
                callbackContext.sendPluginResult(result);
            }};
        mDbWorkerThread.postTask(task);
    }

    private void setUserId(CallbackContext callbackContext, String inUserId) {
        userId = inUserId;
        PluginResult result = new PluginResult(PluginResult.Status.OK);
        callbackContext.sendPluginResult(result);
    }

    private void setBackgroundStatus(CallbackContext callbackContext, Boolean inBackgroundStatus) {
        backgroundStatus = inBackgroundStatus;
        PluginResult result = new PluginResult(PluginResult.Status.OK);
        callbackContext.sendPluginResult(result);
    }

    private void startForegroundService() {
        if (android.os.Build.VERSION.SDK_INT >= 26 && foregroundServiceRunning == false) {
            Activity activity = cordova.getActivity();
            Intent intent = new Intent(activity, ForegroundService.class);

            intent.setAction("start");

            //TODO: Create a better icon than star
            intent.putExtra("title", "Seer")
                    .putExtra("text", "Seer is monitoring your companion button.")
                    .putExtra("icon", "star")
                    .putExtra("importance", "3")
                    .putExtra("id", "90");

            activity.getApplicationContext().startForegroundService(intent);
            foregroundServiceRunning = true;
        }
    }
}
