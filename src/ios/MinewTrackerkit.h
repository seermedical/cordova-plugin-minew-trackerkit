#import <Cordova/CDVPlugin.h>

@interface MinewTrackerkit : CDVPlugin {
}

// The hooks for our plugin commands
- (void)bleStatus:(CDVInvokedUrlCommand *)command;
- (void)startScan:(CDVInvokedUrlCommand *)command;
- (void)bind:(CDVInvokedUrlCommand *)command;

@end
