//
//  Category.h
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CFEntry.h"

@interface CFCategory : CFEntry

@property (strong, nonatomic) NSString* category_name;
@property (strong, nonatomic) NSString* category_type;

- (id) initWithId: (NSNumber*) id withName: (NSString*) name withType: (NSString*) type;

@end
