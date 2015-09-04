//
//  LayoutViewController.h
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/4/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFCompanyData.h"
#import <UIKit/UIKit.h>

@interface LayoutViewController : UIViewController <UIActionSheetDelegate>

@property (nonatomic, strong) CFCompanyData* selectedCompany;

@end
