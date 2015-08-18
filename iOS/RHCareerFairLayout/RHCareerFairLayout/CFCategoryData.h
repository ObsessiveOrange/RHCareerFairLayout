//
//  CFCategoryData.h
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/9/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CFCategory.h"

@interface CFCategoryData : CFCategory

@property (nonatomic) bool category_selected;

-(id) initWithId: (NSInteger) id withName: (NSString*) name withType: (NSString*) type selected: (bool) selected;

@end
