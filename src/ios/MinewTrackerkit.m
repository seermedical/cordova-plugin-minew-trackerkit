#import "MinewTrackerkit.h"

#import <Cordova/CDVAvailability.h>
#import <MTTrackit/MTTrackit.h>

@implementation MinewTrackerkit

- (void)bleStatus:(CDVInvokedUrlCommand *)command {
  NSString* test = "hello";
  CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:test];
  [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


@end
