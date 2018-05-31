#import <Cordova/CDVPlugin.h>
#import <MTTrackit/MTTrackit.h>

@interface MinewTrackerkit : CDVPlugin {
}

@property (strong, nonatomic) NSMutableSet *peripherals;
@property (strong, nonatomic) MTTrackerManager *manager;

// The hooks for our plugin commands
- (void)bleStatus:(CDVInvokedUrlCommand *)command;
- (void)startScan:(CDVInvokedUrlCommand *)command;
- (void)stopScan:(CDVInvokedUrlCommand *)command;
- (void)connect:(CDVInvokedUrlCommand *)command;
- (void)subscribe:(CDVInvokedUrlCommand *)command;

@end
