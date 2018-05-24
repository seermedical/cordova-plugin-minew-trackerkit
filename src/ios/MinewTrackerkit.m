#import "MinewTrackerkit.h"

#import <Cordova/CDVAvailability.h>
#import <MTTrackit/MTTrackit.h>

@implementation MinewTrackerkit

@synthesize manager;
@synthesize trackers;

- (void)pluginInitialize {
  [super pluginInitialize];
  trackers = [NSMutableSet set];
  manager = [MTTrackerManager sharedInstance];
}

- (void)bleStatus:(CDVInvokedUrlCommand *)command {
  MTTrackerManager *manager = [MTTrackerManager sharedInstance];
  BOOL ble;
  NSLog(@"%@",manager.bleState);
  if(manager.bleState == Poweron) {
    ble = true;
  } else {
    ble = false;
  }
  CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:ble];
  [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

-(void)stopScan:(CDVInvokedUrlCommand *)command {
  MTTrackerManager *manager = [MTTrackerManager sharedInstance];
  [manager stopScan];
}

- (void)startScan:(CDVInvokedUrlCommand *)command {
  MTTrackerManager *manager = [MTTrackerManager sharedInstance];
  // start scanning task
  [manager startScan:^(NSArray<MTTracker *> *trackers){
    NSInteger N = [trackers count];
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithCapacity:10];
    for(NSInteger i = 0; i < N; i ++){
      MTTracker *tracker = trackers[i];
      [trackers addObject:tracker];

      NSString *mac = tracker.mac; // mac address
      NSString *name = tracker.name; // bluetooth name
      NSInteger rssi = tracker.rssi;   // RSSI
      NSInteger battery = tracker.battery; // battery 0～100
      Connection status = tracker.connection; // current connection status
      ModelType model = tracker.model;    // the tracker's model
      DistanceLevel dis = tracker.distance;    // distance information.

      dict[@"address"] = mac;
      dict[@"name"] = name;
      // dict[@"battery"] = battery;

      NSLog(@"connection status: %ld",status);
      NSLog(@"model type: %ld",model);
      NSLog(@"distance: %ld",dis);

      CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dict];
      [result setKeepCallback:[NSNumber numberWithBool:YES]];
      [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
  }];
}

-(void)bind:(CDVInvokedUrlCommand *)command {
  NSString* id = [command.arguments objectAtIndex:0];
}


@end
