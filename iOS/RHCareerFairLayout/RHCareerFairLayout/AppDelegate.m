//
//  AppDelegate.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/4/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "AppDelegate.h"
#import "DBManager.h"
#import "RHCareerFairLayout-Swift.h"
#import <FMDB.h>
#import <Google/Analytics.h>

@interface AppDelegate ()

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    
    [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent animated:false];
    
    // Setup forceReload flag - defaults to false unless set otherwise.
    self.forceReload = false;
    
    // Leave highlighted table as nil until needed.
    self.hightlightTableId = nil;
    
    // Setup searchText - default it to an empty string on first run.
    if(![[NSUserDefaults standardUserDefaults] stringForKey:@"searchText"]){
        [[NSUserDefaults standardUserDefaults] setValue:@"" forKey:@"searchText"];
    }
    
    // Setup DB. Create if needed.
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES);
    NSString *docsPath = [paths objectAtIndex:0];
    NSString *path = [docsPath stringByAppendingPathComponent: RHCareerFairLayout.dbName];
    
    // Create and save DB reference
    RHCareerFairLayout.database = [FMDatabase databaseWithPath:path];
    [DBManager setupDB];
    
    // Configure tracker from GoogleService-Info.plist.
    NSError *configureError;
    [[GGLContext sharedInstance] configureWithError:&configureError];
    NSAssert(!configureError, @"Error configuring Google services: %@", configureError);
    
    // Configure GAI options.
    GAI *gai = [GAI sharedInstance];
    gai.dispatchInterval = 30;
    gai.trackUncaughtExceptions = YES;  // report uncaught exceptions
    // gai.logger.logLevel = kGAILogLevelVerbose;  // for dubugging purposes
    
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

@end
