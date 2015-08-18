//
//  DBManager.h
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/6/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CFDataWrapper.h"
#import "CFTableDataMap.h"
#import "CFCompanyData.h"

@interface DBManager : NSObject

+ (void) setupDB;
+ (void) resetDB;
+ (void) createTables;
+ (void) dropTables;
+ (void) dropCategories;
+ (void) dropViews;

+ (void) loadNewData: (CFDataWrapper*) data;
+ (NSArray*) getFilteredCompanies;
+ (CFCompanyData*) getCompanyWithId: (NSInteger) companyId;
+ (void) updateFilteredCompaniesWithSelected: (bool)selected;
+ (NSArray*) getCategories;
+ (NSDictionary*) getCategoriesForCompany: (NSInteger) companyId;
+ (void) setCompany: (NSInteger) companyId selected:(bool)selected;
+ (void) setCategory: (NSInteger) categoryId selected:(bool)selected;
+ (void) clearSelectedCategories;
+ (CFTableDataMap*) getTables;
+ (CFTerm*) getTerm;
@end
