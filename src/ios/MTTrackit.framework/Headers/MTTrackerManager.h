//
//  MTTrackerManager.h
//  Tracker
//
//  Created by SACRELEE on 10/31/17.
//  Copyright © 2017 MinewTech. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MTTracker.h"

@class MTTracker;

// current bluetooth status
typedef NS_ENUM(NSUInteger, BluetoothState) {
    Unknown = 0,
    Resetting,
    Unsupported,
    Unauthorized,
    Poweroff,
    Poweron,
};

typedef void(^ScanBlock)(NSArray<MTTracker *> *trackers);
typedef void(^TrackerConBlock)(Connection con, MTTracker *tracker);
typedef void(^StateChangeBlock)(BluetoothState state);


@interface MTTrackerManager: NSObject

// Scanned Trackers
@property (nonatomic, strong, readonly) NSArray<MTTracker *> *scannedTrackers;

// bind Trackers
@property (nonatomic, strong, readonly) NSArray<MTTracker *> *bindTrackers;

// bluetooth state
@property (nonatomic, assign, readonly) BluetoothState bleState;

// bind password for trackers.
@property (nonatomic, strong) NSString *password;


/**
 get a shared instance

 */
+ (instancetype)sharedInstance;

/**
 listen the change of bluetooth state
 
 @param handler state changes block
 */
- (void)didChangeBluetooth:(StateChangeBlock)handler;


/**
 listen the change of connection
 
 @param handler connection changes block
 */
- (void)didChangeConnection:(TrackerConBlock)handler;

/**
  Start scan for trackers

 @param completionHandler scan block
 */
- (void)startScan:(ScanBlock)completionHandler;

/**
  stop scan for trackers
 */
- (void)stopScan;

/**
 validate a tracker for first bind

 @param tracker the tracker for validate
 @param handler validate block
 */
- (void)bindingVerify:(MTTracker *)tracker completion:(void(^)(BOOL success, NSError *error))handler;

/**
 unbind a tracker with mac address, get a result in block.

 @param mac a the mac address of a tracker
 @param handler get a result in block.
 */
- (void)unbindTracker:(NSString *)mac completion:(OperationBlock)handler;


/**
 bind a tracker with mac address.
 you can bind a tracker with mac address.
 please note that you should make sure the mac you give is a "verified tracker". it means you have used
 method "- (void)bindingVerify:(MTTracker *)tracker completion:(void(^)(BOOL success, NSError *error))handler;" and get a success = YES result.
 
 @param mac mac of tracker
 @return a tracker instance.
 */
- (MTTracker *)addTracker:(NSString *)mac;


/**
 remove a tracker
 
 @param mac the mac of track which will be unbind.
 */
- (void)removeTracker:(NSString *)mac;

/**
 unbind all bind trackers.
 */
- (void)removeAllTrackers;

@end
