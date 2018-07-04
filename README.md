Minew-TrackerKit
======

From Cordova plugin template from here https://github.com/rrostt/cordova-plugin-template
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

# Android Quirks
* permissions - don't start scanning straight away. The user needs to grant location access first.

# Minew Resources

- [SDK for android and ios](http://docs.beaconyun.com/TrackerKit/iOS_MinewTrackerKit_Software_Development_Kit_Guide_en/)
- [Minew Libraries and Framework](https://api.beaconyun.com/d/ba7627b8b03f4cb6a4a1/?p=/iOS&mode=list)

# General Resources

- [BLE template](https://github.com/seermedical/cordova-plugin-ble-central)
- [Cordova documentation](https://cordova.apache.org/docs/en/latest/guide/hybrid/plugins/)
