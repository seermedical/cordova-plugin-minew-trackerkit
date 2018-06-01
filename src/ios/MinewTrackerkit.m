#import "MinewTrackerkit.h"

#import <Cordova/CDVAvailability.h>
#import <MTTrackit/MTTrackit.h>

@implementation MinewTrackerkit

@synthesize manager;
@synthesize peripherals;

- (void)pluginInitialize {

    [super pluginInitialize];

    peripherals = [NSMutableSet set];
    manager = [MTTrackerManager sharedInstance];
    manager.password = @"B3agle!!";
}

- (void)stopScan:(CDVInvokedUrlCommand *)command {
  [manager stopScan];
}

- (void)startScan:(CDVInvokedUrlCommand *)command {
  // MTTrackerManager *manager = [MTTrackerManager sharedInstance];
  [manager startScan:^(NSArray<MTTracker *> *trackers){
    NSInteger N = [trackers count];
    for(NSInteger i = 0; i < N; i ++){
      MTTracker *tracker = trackers[i];
      [peripherals addObject:tracker];
      NSMutableDictionary *dictionary = [self asDictionary:tracker];

      CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dictionary];
      [result setKeepCallback:[NSNumber numberWithBool:YES]];
      [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
  }];
}

- (void)find:(CDVInvokedUrlCommand *)command {
  // THIS FUNCTION WILL LOOK FOR ALREADY BOUND TRACKERS ON APP RESTART
}

- (void)connect:(CDVInvokedUrlCommand *)command {
  NSString* id = [command.arguments objectAtIndex:0];
  NSPredicate *predicate = [NSPredicate predicateWithFormat:@"mac == %@", id];
  NSSet *trackers = [peripherals filteredSetUsingPredicate:predicate];
  NSArray *array = [trackers allObjects];
  // NSLog(@"number of periperhals: %d",[array count]);
  MTTracker *trackerToBind = [array objectAtIndex:0];
  [manager bindingVerify:trackerToBind completion:^(BOOL success, NSError *error) {
    CDVPluginResult *result;
    if (success) {
      result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } else {
      result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
  }];
}

- (void)disconnect:(CDVInvokedUrlCommand *)command {
  NSString* id = [command.arguments objectAtIndex:0];
  // unbind the tracker
  [manager unbindTracker:id completion:^(BOOL success, NSError *error) {
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:success];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
  }];
}

- (void)subscribe:(CDVInvokedUrlCommand *)command {
  NSString* id = [command.arguments objectAtIndex:0];
  NSPredicate *predicate = [NSPredicate predicateWithFormat:@"mac == %@", id];
  NSSet *trackers = [peripherals filteredSetUsingPredicate:predicate];
  NSArray *array = [trackers allObjects];
  MTTracker *trackerToSubscribe = [array objectAtIndex:0];
  Connection status = trackerToSubscribe.connection;
  if (status == ConnectionConnected) {
    [trackerToSubscribe didReceive:^(Receiving rec) {
        if(rec == ReceivingButtonPushed) {
          CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:id];
          [result setKeepCallback:[NSNumber numberWithBool:YES]];
          [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }
    }];
  } else {
    // TODO connect then bind
  }
}

- (NSMutableDictionary *)asDictionary:(MTTracker *)tracker {
  NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];

  NSString *mac = tracker.mac; // mac address
  NSString *name = tracker.name; // bluetooth name
  NSInteger rssi = tracker.rssi;   // RSSI
  NSInteger battery = tracker.battery; // battery 0～100
  Connection status = tracker.connection; // current connection status
  ModelType model = tracker.model;    // the tracker's model
  DistanceLevel distance = tracker.distance;    // distance information.

  [dictionary setObject: mac forKey: @"address"];
  if (name) {
    [dictionary setObject: name forKey: @"name"];
  }
  if (rssi) {
    [dictionary setObject: [NSNumber numberWithInt:rssi] forKey: @"rssi"];
  }
  if (battery) {
    [dictionary setObject: [NSNumber numberWithInt:battery] forKey: @"battery"];
  }

  switch (status) {
    case ConnectionConnectFailed:
      [dictionary setObject: @"failed" forKey: @"connection"];
      break;

    case ConnectionDisconnected:
      [dictionary setObject: @"disconnected" forKey: @"connection"];
      break;

    case ConnectionConnecting:
      [dictionary setObject: @"connecting" forKey: @"connection"];
      break;

    case ConnectionConnected:
      [dictionary setObject: @"connected" forKey: @"connection"];
      break;

    default:
      [dictionary setObject: [NSNull null] forKey: @"connection"];
      break;
  }

  switch (distance) {
    case DistanceLevelUndefined:
      [dictionary setObject: [NSNull null] forKey: @"distance"];
      break;

    case DistanceLevelValidating:
      [dictionary setObject: [NSNull null] forKey: @"distance"];
      break;

    case DistanceLevelFar:
      [dictionary setObject: @"far" forKey: @"distance"];
      break;

    case DistanceLevelMiddle:
      [dictionary setObject: @"mid" forKey: @"distance"];
      break;

    case DistanceLevelNear:
      [dictionary setObject: @"near" forKey: @"distance"];
      break;

    default:
      [dictionary setObject: [NSNull null] forKey: @"distance"];
      break;
  }

  switch (model) {
    case ModelTypeNone:
      [dictionary setObject: [NSNull null] forKey: @"model"];
      break;

    case ModelTypeF4S:
      [dictionary setObject: @"F4S" forKey: @"model"];
      break;

    default:
      [dictionary setObject: [NSNull null] forKey: @"model"];
      break;
  }

  return dictionary;
}


@end
