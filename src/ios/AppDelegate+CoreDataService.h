
//
//  AppDelegate+CoreDataService.h
//
//
//

#import "AppDelegate.h"

@interface AppDelegate (CULPlugin)

- (void) _swizzle_applicationWillTerminate:(UIApplication *)application;

@end
