//
//  MTTracker.h
//  Tracker
//
//  Created by SACRELEE on 10/31/17.
//  Copyright © 2017 MinewTech. All rights reserved.
//

#import <Foundation/Foundation.h>

// 
typedef NS_ENUM(NSUInteger, Receiving) {
    ReceivingUndefined = 0,
    ReceivingButtonPushed,
};

// current connection status
typedef NS_ENUM(NSUInteger, Connection) {
    ConnectionConnectFailed = -1,
    ConnectionDisconnected = 0,
    ConnectionConnecting,
    ConnectionConnected,
};

// the distance between tracker and phone.
typedef NS_ENUM(NSUInteger, DistanceLevel) {
    DistanceLevelUndefined = 0,
    DistanceLevelValidating,
    DistanceLevelFar,
    DistanceLevelMiddle,
    DistanceLevelNear,
};

// Model type, such as: F4S ...
typedef NS_ENUM(NSInteger, ModelType) {
    ModelTypeNone = -1,
    ModelTypeF4S,
};

typedef void(^ReceivingBlock)(Receiving rec);
typedef void(^ConnectionChangeBlock)(Connection con);
typedef void(^OperationBlock)(BOOL success, NSError *error);
typedef void(^ValueUpdateBlock)(void);


@interface MTTracker : NSObject

// ble name
@property (nonatomic, strong, readonly) NSString *name;

// ble mac address
@property (nonatomic, strong, readonly) NSString *mac;

// ble rssi
@property (nonatomic, assign, readonly) NSInteger rssi;

// battery of device
@property (nonatomic, assign, readonly) NSInteger battery;

// radio txpower
@property (nonatomic, assign, readonly) NSInteger radioTx;

// distance from rssi
@property (nonatomic, assign, readonly) DistanceLevel distance;

// the date update by last advertisement.
@property (nonatomic, strong, readonly) NSDate *advUpdate;

// current connection.
@property (nonatomic, assign, readonly) Connection connection;

// model type
@property (nonatomic, assign, readonly) ModelType model;

/**
 let the tracker ring or not.

 @param ring ring the tracker or not
 @param handler operation success or not.
 */
- (void)switchBellStatus:(BOOL)ring completion:(OperationBlock)handler;

/**
 config the settings alarm or not when device disconnect from iphone

 @param alert if yes, device will alarm when disconnect.
 @param handler operation success or not.
 */
- (void)writeLossAlert:(BOOL)alert completion:(OperationBlock)handler;

/**
 config the disconnection distance between device and iphone.

 @param level far middle or near
 @param handler operation success or not.
 */
- (void)writeLossDistance:(DistanceLevel)level completion:(OperationBlock)handler;

/**
 config the delay of disconnection.

 @param delay after delay time will call back device disconnected.
 @param handler operation success or not.
 */
- (void)writeLossDelay:(NSInteger)delay completion:(OperationBlock)handler;


/**
 !!!! this is a call back method.
 it will call back when the connection changes.
 
 @param handler connection change block.
 */
- (void)didConnectionChange:(ConnectionChangeBlock)handler;


/**
 !!!! this is a call back method.
 it will call back when the tracker received a new instruction.
 
 @param handler receiving block.
 */
- (void)didReceive:(ReceivingBlock)handler;


/**
 !!!! this is a call back method.
 it will call back when the tracker updated its value.

 @param handler value update block.
 */
- (void)didValueUpdate:(ValueUpdateBlock)handler;

@end
