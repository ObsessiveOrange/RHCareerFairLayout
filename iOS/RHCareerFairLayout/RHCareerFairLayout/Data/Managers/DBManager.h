//
//  DBManager.h
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/6/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CFDataWrapper.h"

@interface DBManager : NSObject

+ (void) setupDB;
+ (void) resetDB;
+ (void) createTables;
+ (void) dropTables;
+ (void) dropCategories;
+ (void) dropViews;

+ (void) loadNewData: (CFDataWrapper*) data;
+ (NSArray*) getFilteredCompanies;
+ (void) updateAllSelectedCompaniesWithSelected: (bool)selected;
+ (NSArray*) getCategories;
+ (void) setCompany: (NSNumber*) companyId selected:(bool)selected;
+ (void) setCategory: (NSNumber*) categoryId selected:(bool)selected;
+ (void) clearSelectedCategories;

@end
