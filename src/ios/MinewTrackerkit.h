#import <Cordova/CDVPlugin.h>
#import <MTTrackit/MTTrackit.h>

@interface MinewTrackerkit : CDVPlugin {
}

@property (strong, nonatomic) NSMutableSet *peripherals;
@property (strong, nonatomic) MTTrackerManager *manager;

// The hooks for our plugin commands
- (void)find:(CDVInvokedUrlCommand *)command;
- (void)startScan:(CDVInvokedUrlCommand *)command;
- (void)stopScan:(CDVInvokedUrlCommand *)command;
- (void)connect:(CDVInvokedUrlCommand *)command;
- (void)disconnect:(CDVInvokedUrlCommand *)command;
- (void)subscribeToClick:(CDVInvokedUrlCommand *)command;
- (void)subscribeToStatus:(CDVInvokedUrlCommand *)command;
- (NSMutableDictionary *)asDictionary:(MTTracker *)tracker;

@end
