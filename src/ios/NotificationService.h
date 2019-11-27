#import <Foundation/Foundation.h>
@import UserNotifications;

@interface NotificationService : NSObject {
}

@property (strong, nonatomic) UNUserNotificationCenter *notificationCenter;

+(NotificationService*)getSharedInstance;
-(void)createNotificationService;
-(void)sendNotification:(NSString*)title message:(NSString*)message;

@end
