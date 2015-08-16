//
//  CompanyCategory.h
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CFEntry.h"

@interface CFCompanyCategory : NSObject

@property (nonatomic, strong) NSNumber* companyCategory_companyId;
@property (nonatomic, strong) NSArray* companyCategory_categories;

- (id) initWithCompanyId: (NSNumber*) companyId withCategories: (NSArray*) categories;

@end
