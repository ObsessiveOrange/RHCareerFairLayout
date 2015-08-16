//
//  TableMapping.h
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CFEntry.h"

@interface CFTableMapping : CFEntry
@property (nonatomic, strong) NSNumber* tableMapping_companyId;
@property (nonatomic, strong) NSNumber* tableMapping_size;

- (id) initWithId: (NSNumber*) id withCompanyId: (NSNumber*) companyId withSize: (NSNumber*) size;

@end
