Minew-TrackerKit
======

# Installation

`cordova plugin add https://github.com/seermedical/cordova-plugin-minew-trackerkit.git`

A Cordova plugin on iOS and Android to communicate with the Minew TrackerKit Framework

Plugin development environment: https://github.com/seermedical/seer-beacon-tracker

# Plugin structure
* central manager
* central array of buttons that can be easily identified and accessed by all functions

# Buttons
* address: mac address (unique ID)
* name: could be null
* model: F4S or Finder or null
* RSSI: received signal strength
* battery: int between 0-100 for battery level
* distance: near, mid, far (currently only available on iOS)
* status: connected, disconnected, connecting, failed

# Plugin functions
## startScan(success, failure)
* locate buttons and add to central array
* success - receieves buttons when they are found
* failure - currently only applies to android (no permission)

### TODO
* should accept scanTime (and stopScan after elapsed time)
* put in fail callbakc

## stopScan(success, failure)
* stop currently running scan

## find(address, success, failure)
* look for previously bound beacon address and connect
* success - callback on connection
* failure - callback if connection fails

### TODO
* functions are working as intended via a workaround but the internal code was not working as expected from the Minew SDK. Need to followup with Minew.

## connect(address, success, failure)
* connect to a button for mac address
* success - callback on connection
* failure - callback if connection fails

## disconnect(address, success, failure)
* remove button for mac address
* success - called on success
* success - called on failure

## subscribeToClick(address, success, failure)
* listen to beacon click
* success - gets called every click
* failure - gets called if it fails to subscribe

## subscribeToStatus(address, success, failure)
* listen to beacon connection status
* success - gets called on reconnection
* failure - gets called on disconnection

# iOS Quirks

* you need to strip architectures from the connected MTTrackit framework before it will upload to the app store.

This script should be added as a run script to XCode (see [this answer](https://stackoverflow.com/questions/30547283/submit-to-app-store-issues-unsupported-architecture-x86)). To add run script in XCode go to project target, select 'Build Phases' tab, choose Editor -> Add Build Phase -> Add Run Script Build Phase, Click on the newly created Run Script, copy in the following code:

```
echo "Target architectures: $ARCHS"

APP_PATH="${TARGET_BUILD_DIR}/${WRAPPER_NAME}"

find "$APP_PATH" -name '*.framework' -type d | while read -r FRAMEWORK
do
FRAMEWORK_EXECUTABLE_NAME=$(defaults read "$FRAMEWORK/Info.plist" CFBundleExecutable)
FRAMEWORK_EXECUTABLE_PATH="$FRAMEWORK/$FRAMEWORK_EXECUTABLE_NAME"
echo "Executable is $FRAMEWORK_EXECUTABLE_PATH"
echo $(lipo -info "$FRAMEWORK_EXECUTABLE_PATH")

FRAMEWORK_TMP_PATH="$FRAMEWORK_EXECUTABLE_PATH-tmp"

# remove simulator's archs if location is not simulator's directory
case "${TARGET_BUILD_DIR}" in
*"iphonesimulator")
    echo "No need to remove archs"
    ;;
*)
    if $(lipo "$FRAMEWORK_EXECUTABLE_PATH" -verify_arch "i386") ; then
    lipo -output "$FRAMEWORK_TMP_PATH" -remove "i386" "$FRAMEWORK_EXECUTABLE_PATH"
    echo "i386 architecture removed"
    rm "$FRAMEWORK_EXECUTABLE_PATH"
    mv "$FRAMEWORK_TMP_PATH" "$FRAMEWORK_EXECUTABLE_PATH"
    fi
    if $(lipo "$FRAMEWORK_EXECUTABLE_PATH" -verify_arch "x86_64") ; then
    lipo -output "$FRAMEWORK_TMP_PATH" -remove "x86_64" "$FRAMEWORK_EXECUTABLE_PATH"
    echo "x86_64 architecture removed"
    rm "$FRAMEWORK_EXECUTABLE_PATH"
    mv "$FRAMEWORK_TMP_PATH" "$FRAMEWORK_EXECUTABLE_PATH"
    fi
    ;;
esac

echo "Completed for executable $FRAMEWORK_EXECUTABLE_PATH"
echo $(lipo -info "$FRAMEWORK_EXECUTABLE_PATH")

done
```

# Android Quirks
* permissions - don't start scanning straight away. The user needs to grant location access first.

# Minew Resources

- [SDK for android and ios](http://docs.beaconyun.com/TrackerKit/iOS_MinewTrackerKit_Software_Development_Kit_Guide_en/)
- [Minew Libraries and Framework](https://api.beaconyun.com/d/ba7627b8b03f4cb6a4a1/?p=/iOS&mode=list)

# General Resources

- [BLE template](https://github.com/seermedical/cordova-plugin-ble-central)
- [Cordova documentation](https://cordova.apache.org/docs/en/latest/guide/hybrid/plugins/)
- [Cordova plugin template](https://github.com/rrostt/cordova-plugin-template)
