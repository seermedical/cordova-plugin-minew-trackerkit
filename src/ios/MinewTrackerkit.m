#import "MinewTrackerkit.h"

#import <Cordova/CDVAvailability.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import <MTTrackit/MTTrackit.h>
#import "NotificationService.h"
#import "Seer-Swift.h"

@implementation MinewTrackerkit

@synthesize manager;
@synthesize peripherals;
@synthesize coreDataService;
@synthesize notificationService;
@synthesize userId;

- (void)pluginInitialize {
    [super pluginInitialize];
    manager = [MTTrackerManager sharedInstance];
    manager.password = @"B3agle!!";
    
    coreDataService = [OCCoreDataService getSharedInstance];
    notificationService = [NotificationService getSharedInstance];
}

- (void)stopScan:(CDVInvokedUrlCommand *)command {
    [manager stopScan];
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)startScan:(CDVInvokedUrlCommand *)command {
    if(manager.bleState == Poweron) {
        peripherals = [NSMutableSet set];
        [manager startScan:^(NSArray<MTTracker *> *trackers){
            NSInteger N = [trackers count];
            for(NSInteger i = 0; i < N; i ++){
                
                MTTracker *tracker = trackers[i];
                [self->peripherals addObject:tracker];
                NSMutableDictionary *dictionary = [self asDictionary:tracker];

                CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dictionary];
                [result setKeepCallback:[NSNumber numberWithBool:YES]];
                [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            }
        }];
    }
}

- (void)find:(CDVInvokedUrlCommand *)command {
    NSString* id = [command.arguments objectAtIndex:0];
    peripherals = [NSMutableSet set];
    
    [manager startScan:^(NSArray<MTTracker *> *trackers){
        NSInteger N = [trackers count];
        for(NSInteger i = 0; i < N; i ++){
            MTTracker *tracker = trackers[i];
            NSString *mac = tracker.mac; // mac address
            if ([mac isEqualToString:id]) {
                
                [self->peripherals addObject:tracker];
                [self->manager stopScan];
                NSMutableDictionary *dictionary = [self asDictionary:tracker];
                [self->manager bindingVerify:tracker completion:^(BOOL success, NSError *error) {
                    CDVPluginResult *result;
                    if (success) {
                        [self->notificationService sendNotification:@"Connected" message:@"Your Seer companion button is connected and ready to use."];
                        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dictionary];
                    } else {
                        [self->notificationService sendNotification:@"Connection Failed" message:@"Your Seer companion button is not connected."];
                        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"connection failed"];
                    }
                    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                }];
            }
        }
    }];
}

- (void)connect:(CDVInvokedUrlCommand *)command {
    NSString* id = [command.arguments objectAtIndex:0];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"mac == %@", id];
    NSSet *trackers = [peripherals filteredSetUsingPredicate:predicate];
    NSArray *array = [trackers allObjects];
    
    NSInteger N = [trackers count];
    __block CDVPluginResult *result;
    if (N < 1) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"cant find id"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    } else {
        MTTracker *trackerToBind = [array objectAtIndex:0];
        [manager bindingVerify:trackerToBind completion:^(BOOL success, NSError *error) {
            if (success) {
                [self->notificationService sendNotification:@"Connected" message:@"Your Seer companion button is connected and ready to use."];
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            } else {
                [self->notificationService sendNotification:@"Connection Failed" message:@"Your Seer companion button is not connected."];
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"connection failed"];
            }
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }];
    }
}

- (void)disconnect:(CDVInvokedUrlCommand *)command {
    NSString* id = [command.arguments objectAtIndex:0];

    [manager unbindTracker:id completion:^(BOOL success, NSError *error) {
        // success YES means operate success, else NO.
    }];
    [manager removeTracker:id];
    [self pluginInitialize];
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)subscribeToClick:(CDVInvokedUrlCommand *)command {

    NSString* id = [command.arguments objectAtIndex:0];
    
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"mac == %@", id];
    NSSet *trackers = [peripherals filteredSetUsingPredicate:predicate];
    NSArray *array = [trackers allObjects];
    NSInteger N = [trackers count];
    if (N < 1) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"cant find id"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    } else {
        MTTracker *trackerToSubscribe = [array objectAtIndex:0];
        Connection status = trackerToSubscribe.connection;
        if (status == ConnectionConnected) {
            [trackerToSubscribe didReceive:^(Receiving rec) {
                if(rec == ReceivingButtonPushed) {
                    
                    [self->notificationService sendNotification:@"Seer" message:@"Your event has been recorded. Open the Seer app to add more details."];
                    if([[UIApplication sharedApplication] applicationState] == UIApplicationStateActive) {
                        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:id];
                        [result setKeepCallback:[NSNumber numberWithBool:YES]];
                        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                    } else {                       
                        if(self->userId) {
                            double secsUtc1970 = [[NSDate date]timeIntervalSince1970];
                            NSNumber *myDoubleNumber = [NSNumber numberWithDouble:secsUtc1970];
                            
                            [self->coreDataService insertButtonDataWithUserId:(self->userId) startTime:(secsUtc1970 * 1000) createdAt:([NSDate date]) timezone:[[NSTimeZone localTimeZone] secondsFromGMT] / 3600.0];
                        }
                    }
                }
            }];
        } else {
            // TODO connect then bind
            [self->notificationService sendNotification:@"Connection Failed" message:@"Your Seer companion button is not connected."];
            CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"connection failed"];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }
    }
}

- (void)subscribeToStatus:(CDVInvokedUrlCommand *)command {

    NSString* id = [command.arguments objectAtIndex:0];
    
    // TODO make this search its own function?
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"mac == %@", id];
    NSSet *trackers = [peripherals filteredSetUsingPredicate:predicate];
    NSArray *array = [trackers allObjects];
    NSInteger N = [trackers count];
    if (N < 1) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"cant find id"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    } else {
        MTTracker *trackerToSubscribe = [array objectAtIndex:0];
        [trackerToSubscribe didConnectionChange:^(Connection con){
            switch(con){
                case ConnectionConnecting:
                    NSLog(@"Connecting to the tracker");
                    break;
                case ConnectionConnected: {
                    NSLog(@"Tracker is connected");
                    CDVPluginResult *resultConnect = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                    [resultConnect setKeepCallback:[NSNumber numberWithBool:YES]];
                    [self.commandDelegate sendPluginResult:resultConnect callbackId:command.callbackId];
                    break;
                }
                case ConnectionDisconnected: {
                    NSLog(@"Tracker is disconnected");
                    [self->notificationService sendNotification:@"Disconnected" message:@"Your Seer Button is disconnected."];
                    CDVPluginResult *resultDisconnect = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
                    [resultDisconnect setKeepCallback:[NSNumber numberWithBool:YES]];
                    [self.commandDelegate sendPluginResult:resultDisconnect callbackId:command.callbackId];
                    break;
                }
                case ConnectionConnectFailed: {
                    NSLog(@"Tracker connection failed");
                    CDVPluginResult *resultDisconnect = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
                    [resultDisconnect setKeepCallback:[NSNumber numberWithBool:YES]];
                    [self.commandDelegate sendPluginResult:resultDisconnect callbackId:command.callbackId];
                    break;
                }
            };
        }];
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

- (void)fetchButtonData:(CDVInvokedUrlCommand *)command {
    if(userId) {        
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray: [coreDataService fetchButtonDataWithUserId:(userId)]];
        [result setKeepCallback:[NSNumber numberWithBool:YES]];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
}

- (void)deleteButtonData:(CDVInvokedUrlCommand *)command {
    if(userId) {
        [coreDataService deleteButtonDataWithUserId:(userId)];
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [result setKeepCallback:[NSNumber numberWithBool:YES]];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
}

- (void)setUserId:(CDVInvokedUrlCommand *)command {
    userId = [command.arguments objectAtIndex:0];
}


@end
