//
//  TabBarController.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "TabBarController.h"
#import "RHCareerFairLayout-Swift.h"
#import <FontAwesomeKit/FontAwesomeKit.h>

@interface TabBarController ()

@end

@implementation TabBarController

static TabBarController* instance;

+ (TabBarController*) instance {
    return instance;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.tabBar.barTintColor = RHCareerFairLayout.color_primary;
    self.tabBar.tintColor = RHCareerFairLayout.color_tabText;
    self.tabBar.selectedImageTintColor = RHCareerFairLayout.color_tabTextSelected;
    
    instance = self;
    
}

@end
