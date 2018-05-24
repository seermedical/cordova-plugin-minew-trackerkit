#import <UIKit/UIKit.h>
#import <Cordova/CDVPlugin.h>

@interface CDVMyPlugin : CDVPlugin {}

- (void)ping:(CDVInvokedUrlCommand*)command;

@end
