#import "MinewTrackerkit.h"

#import <Cordova/CDVAvailability.h>
#import <MTTrackit/MTTrackit.h>

@implementation MinewTrackerkit

- (void)bleStatus:(CDVInvokedUrlCommand *)command {
  MTTrackerManager *manager = [MTTrackerManager sharedInstance];
  NSLog(@"state: %@",manager.bleState);
  NSString* test = @"hello";
  CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:test];
  [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}



- (void)startScan:(CDVInvokedUrlCommand *)command {
  MTTrackerManager *manager = [MTTrackerManager sharedInstance];

  if(manager.bleState == Poweron) {
     NSLog(@"the state is power on.");
  }
  // start scanning task.
  [manager startScan:^(NSArray<MTTracker *> *trackers){
      // if manager found devices, this block will call back.
  }];
}

@end
