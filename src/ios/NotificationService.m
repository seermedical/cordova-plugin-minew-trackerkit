#import "NotificationService.h"
@import UserNotifications;

static NotificationService *sharedInstance = nil;

@implementation NotificationService

@synthesize notificationCenter;

+(NotificationService*)getSharedInstance {
    if (!sharedInstance) {
        sharedInstance = [[super allocWithZone:NULL]init];
        [sharedInstance createNotificationService];
    }
    return sharedInstance;
}

-(void)createNotificationService {
    
    notificationCenter = [UNUserNotificationCenter currentNotificationCenter];
    UNAuthorizationOptions options = UNAuthorizationOptionAlert + UNAuthorizationOptionSound;
    
    [notificationCenter requestAuthorizationWithOptions:options
                                      completionHandler:^(BOOL granted, NSError * _Nullable error) {
                                          if (!granted) {
                                              NSLog(@"Something went wrong");
                                          }
                                      }];
}

- (void)setupNotifications {
    
    notificationCenter = [UNUserNotificationCenter currentNotificationCenter];
    UNAuthorizationOptions options = UNAuthorizationOptionAlert + UNAuthorizationOptionSound;
    
    [notificationCenter requestAuthorizationWithOptions:options
                                      completionHandler:^(BOOL granted, NSError * _Nullable error) {
                                          if (!granted) {
                                              NSLog(@"Something went wrong");
                                          }
                                      }];
}

- (void)sendNotification:(NSString*)title message:(NSString*)message {
    UNMutableNotificationContent *content = [UNMutableNotificationContent new];
    content.title = title;
    content.body = message;
    content.sound = [UNNotificationSound defaultSound];
    
    NSString *identifier = @"ButtonNotification";
    UNNotificationRequest *request = [UNNotificationRequest requestWithIdentifier:identifier
                                                                          content:content trigger:nil];
    
    [self cancelNotification];
    [notificationCenter addNotificationRequest:request withCompletionHandler:^(NSError * _Nullable error) {
        if (error != nil) {
            NSLog(@"Something went wrong: %@",error);
        }
    }];
    
}

- (void)cancelNotification {
    NSString *notificationId = @"ButtonNotification";
    UILocalNotification *notification = nil;
    for(UILocalNotification *notify in [[UIApplication sharedApplication] scheduledLocalNotifications])
    {
        if([[notify.userInfo objectForKey:@"ID"] isEqualToString:notificationId])
        {
            notification = notify;
            break;
        }
    }
    [[UIApplication sharedApplication] cancelLocalNotification:notification];
}

@end
