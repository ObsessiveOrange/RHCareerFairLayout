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
@property (nonatomic) NSInteger tableMapping_size;

- (id) initWithId: (NSInteger) id withCompanyId: (NSNumber*) companyId withSize: (NSInteger) size;

@end
