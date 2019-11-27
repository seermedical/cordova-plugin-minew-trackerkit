#import <Cordova/CDVPlugin.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import <MTTrackit/MTTrackit.h>
#import "NotificationService.h"

@class OCCoreDataService;

@interface MinewTrackerkit : CDVPlugin {
}

@property (strong, nonatomic) NSMutableSet *peripherals;
@property (nonatomic, strong) CBCentralManager *bluetoothManager;
@property (strong, nonatomic) MTTrackerManager *manager;
@property (strong, nonatomic) OCCoreDataService *coreDataService;
@property (strong, nonatomic) NotificationService *notificationService;
@property (strong, nonatomic) NSString *userId;

// The hooks for our plugin commands
- (void)find:(CDVInvokedUrlCommand *)command;
- (void)startScan:(CDVInvokedUrlCommand *)command;
- (void)stopScan:(CDVInvokedUrlCommand *)command;
- (void)connect:(CDVInvokedUrlCommand *)command;
- (void)disconnect:(CDVInvokedUrlCommand *)command;
- (void)subscribeToClick:(CDVInvokedUrlCommand *)command;
- (void)subscribeToStatus:(CDVInvokedUrlCommand *)command;
- (void)fetchButtonData:(CDVInvokedUrlCommand *)command;
- (void)deleteButtonData:(CDVInvokedUrlCommand *)command;
- (void)setUserId:(CDVInvokedUrlCommand *)command;
- (NSMutableDictionary *)asDictionary:(MTTracker *)tracker;

@end
