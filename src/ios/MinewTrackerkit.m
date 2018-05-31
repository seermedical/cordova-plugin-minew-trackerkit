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

-(void)stopScan:(CDVInvokedUrlCommand *)command {
  [manager stopScan];
}

- (void)startScan:(CDVInvokedUrlCommand *)command {
  // MTTrackerManager *manager = [MTTrackerManager sharedInstance];
  [manager startScan:^(NSArray<MTTracker *> *trackers){
    NSInteger N = [trackers count];
    for(NSInteger i = 0; i < N; i ++){
      MTTracker *tracker = trackers[i];
      NSString *mac = tracker.mac; // mac address
      [peripherals addObject:tracker];

      // NSString *name = tracker.name; // bluetooth name
      // NSInteger rssi = tracker.rssi;   // RSSI
      // NSInteger battery = tracker.battery; // battery 0～100
      // Connection status = tracker.connection; // current connection status
      // ModelType model = tracker.model;    // the tracker's model
      // DistanceLevel dis = tracker.distance;    // distance information.

      CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:mac];
      [result setKeepCallback:[NSNumber numberWithBool:YES]];
      [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
  }];
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
  // NSLog(@"status: %tu",status);  // WHY IS STATUS ALWAYS 2 !!!!!!!

  [trackerToSubscribe didReceive:^(Receiving rec) {
      if(rec == ReceivingButtonPushed) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:id];
        [result setKeepCallback:[NSNumber numberWithBool:YES]];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
      }
  }];
}


@end
