//
//  AppDelegate+CoreDataService.m
//  Seer
//
//  Created by Anthony Smith on 12/11/19.
//

#import <Foundation/Foundation.h>
#import "AppDelegate+CoreDataService.h"
#import <objc/runtime.h>
#import "Seer-Swift.h"

@implementation AppDelegate (AppDelegateCoreData)

+ (void)load {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        Class class = [self class];

        SEL originalSelector = @selector(applicationWillTerminate:);
        SEL swizzledSelector = @selector(_swizzle_applicationWillTerminate:);
        SEL replacedSelector = @selector(_replace_applicationWillTerminate:);


        Method originalMethod = class_getInstanceMethod(class, originalSelector);
        Method swizzledMethod;
        
        //TODO IS THIS THE RIGHT WAY TO HANDLE THE ORIGINAL METHOD NOT EXISTING?
        if(originalMethod) {
         swizzledMethod = class_getInstanceMethod(class, swizzledSelector);
        } else {
            swizzledMethod = class_getInstanceMethod(class, replacedSelector);
        }

        BOOL didAddMethod =
            class_addMethod(class,
                originalSelector,
                method_getImplementation(swizzledMethod),
                method_getTypeEncoding(swizzledMethod));

        if (didAddMethod) {
            class_replaceMethod(class,
                swizzledSelector,
                method_getImplementation(originalMethod),
                method_getTypeEncoding(originalMethod));
        } else {
            method_exchangeImplementations(originalMethod, swizzledMethod);
        }

    });
}


- (void)_swizzle_applicationWillTerminate:(UIApplication *)application {
    [self _swizzle_applicationWillTerminate:application];
    //WE NEED TO CHECK THAT THIS WORKS AS EXPECTED
    [OCCoreDataService.getSharedInstance saveContext];

    UILocalNotification *notification = [[UILocalNotification alloc] init];
    notification.userInfo =  [NSDictionary dictionaryWithObject:@"killed.app" forKey:@"Killed"];
    notification.fireDate = [NSDate dateWithTimeIntervalSinceNow:2]; // Give some time for firing the notification at least 2 seconds.
    notification.alertBody = @"Seer has been closed. Please open the Seer app to reconnect your companion button";
    notification.soundName = UILocalNotificationDefaultSoundName;
    notification.timeZone = [NSTimeZone systemTimeZone];
    [[UIApplication sharedApplication] scheduleLocalNotification:notification];

    sleep(3);
}

- (void)_replace_applicationWillTerminate:(UIApplication *)application {
    //WE NEED TO CHECK THAT THIS WORKS AS EXPECTED
    [OCCoreDataService.getSharedInstance saveContext];

    UILocalNotification *notification = [[UILocalNotification alloc] init];
    notification.userInfo =  [NSDictionary dictionaryWithObject:@"killed.app" forKey:@"Killed"];
    notification.fireDate = [NSDate dateWithTimeIntervalSinceNow:2]; // Give some time for firing the notification at least 2 seconds.
    notification.alertBody = @"Seer has been closed. Please open the Seer app to reconnect your companion button";
    notification.soundName = UILocalNotificationDefaultSoundName;
    notification.timeZone = [NSTimeZone systemTimeZone];
    [[UIApplication sharedApplication] scheduleLocalNotification:notification];

    sleep(3);
}

@end
