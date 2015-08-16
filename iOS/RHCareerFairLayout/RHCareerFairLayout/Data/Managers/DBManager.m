//
//  DBManager.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/6/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "DBManager.h"
#import "RHCareerFairLayout-Swift.h"
#import "CFCategoryDict.h"
#import "CFCompanyCategory.h"
#import "CFCompany.h"
#import "CFTableMappingArray.h"
#import "CFCategory.h"
#import "CFCompany.h"
#import "CFCompanyCategory.h"
#import "CFEntry.h"
#import "CFTableMapping.h"
#import "CFTerm.h"
#import <FMDB.h>

@implementation DBManager

+ (void) setupDB{
    if(RHCareerFairLayout.dbVersion != [[NSUserDefaults standardUserDefaults] integerForKey:@"dbVersion"]){
        [self dropViews];
        [self dropCategories];
        [self dropTables];
        [self createTables];
        [[NSUserDefaults standardUserDefaults] setInteger:RHCareerFairLayout.dbVersion forKey:@"dbVersion"];
    }
}

+ (void) resetDB{
    [self dropTables];
    [self createTables];
    [[NSUserDefaults standardUserDefaults] setInteger:RHCareerFairLayout.dbVersion forKey:@"dbVersion"];
}

+ (void) createTables{
    if([RHCareerFairLayout.database open]){
        [RHCareerFairLayout.database executeUpdate: DBStatements.CREATE_TABLE_CATEGORY];
        [RHCareerFairLayout.database executeUpdate: DBStatements.CREATE_TABLE_COMPANY];
        [RHCareerFairLayout.database executeUpdate: DBStatements.CREATE_TABLE_COMPANYCATEGORY];
        [RHCareerFairLayout.database executeUpdate: DBStatements.CREATE_TABLE_TABLEMAPPING];
        [RHCareerFairLayout.database executeUpdate: DBStatements.CREATE_TABLE_TERM];
        [RHCareerFairLayout.database executeUpdate: DBStatements.CREATE_TABLE_SELECTED_CATEGORIES];
        [RHCareerFairLayout.database executeUpdate: DBStatements.CREATE_TABLE_SELECTED_COMPANIES];
        [RHCareerFairLayout.database executeUpdate: DBStatements.CREATE_VIEW_FILTERED_COMPANIES_BY_MAJOR];
        [RHCareerFairLayout.database executeUpdate: DBStatements.CREATE_VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION];
        [RHCareerFairLayout.database executeUpdate: DBStatements.CREATE_VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE];
        [RHCareerFairLayout.database executeUpdate: DBStatements.CREATE_VIEW_FILTERED_COMPANIES];
        [RHCareerFairLayout.database close];
    }
}

+ (void) dropTables{
    if([RHCareerFairLayout.database open]){
        [RHCareerFairLayout.database executeUpdate: DBStatements.DROP_TABLE_COMPANY];
        [RHCareerFairLayout.database executeUpdate: DBStatements.DROP_TABLE_COMPANYCATEGORY];
        [RHCareerFairLayout.database executeUpdate: DBStatements.DROP_TABLE_TABLEMAPPING];
        [RHCareerFairLayout.database executeUpdate: DBStatements.DROP_TABLE_TERM];
        [RHCareerFairLayout.database executeUpdate: DBStatements.DROP_TABLE_SELECTED_COMPANIES];
        [RHCareerFairLayout.database close];
    }
}

+ (void) dropCategories{
    if([RHCareerFairLayout.database open]){
        [RHCareerFairLayout.database executeUpdate: DBStatements.DROP_TABLE_CATEGORY];
        [RHCareerFairLayout.database executeUpdate: DBStatements.DROP_TABLE_SELECTED_CATEGORIES];
        [RHCareerFairLayout.database close];
    }
}

+ (void) dropViews{
    if([RHCareerFairLayout.database open]){
        [RHCareerFairLayout.database executeUpdate: DBStatements.DROP_VIEW_FILTERED_COMPANIES];
        [RHCareerFairLayout.database executeUpdate: DBStatements.DROP_VIEW_FILTERED_COMPANIES_BY_MAJOR];
        [RHCareerFairLayout.database executeUpdate: DBStatements.DROP_VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE];
        [RHCareerFairLayout.database executeUpdate: DBStatements.DROP_VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION];
        [RHCareerFairLayout.database close];
    }
}

+ (void) loadNewData: (CFDataWrapper*) data{
    
    [self resetDB];
    if([RHCareerFairLayout.database open]){
        [RHCareerFairLayout.database beginTransaction];
        
        
        NSError* error;
        NSString* sql;
        
        // Insert Categories (Ignore on duplicates)
        CFCategoryDict* categories = data.categoryDict;
        for(id key in categories.categoryDict){
            CFCategory* category = (CFCategory*)[categories objectForKey:key];
            
            sql = [[NSString alloc] initWithFormat:@"INSERT OR IGNORE INTO %@ (%@, %@, %@) VALUES(?, ?, ?);",
                   DBStatements.TABLE_CATEGORY_NAME,
                   DBStatements.KEY_ID,
                   DBStatements.KEY_NAME,
                   DBStatements.KEY_TYPE];
            
            [RHCareerFairLayout.database executeUpdate:sql
                                  withErrorAndBindings:&error,
             category.id,
             category.category_name,
             category.category_type];
            
            
            sql = [[NSString alloc] initWithFormat:@"INSERT OR IGNORE INTO %@ (%@, %@) VALUES(?, ?);",
                   DBStatements.TABLE_SELECTED_CATEGORIES_NAME,
                   DBStatements.KEY_CATEGORY_ID,
                   DBStatements.KEY_SELECTED];
            
            [RHCareerFairLayout.database executeUpdate:sql withArgumentsInArray:@[
                                                                                  category.id,
                                                                                  @false]];
        }
        
        // Insert Companies (Fail on duplicates)
        CFCompanyDict* companies = data.companyDict;
        for(id key in companies.companyDict){
            CFCompany* company = (CFCompany*)[companies objectForKey:key];
            
            sql = [[NSString alloc] initWithFormat:@"INSERT INTO %@ (%@, %@, %@, %@, %@) VALUES(?, ?, ?, ?, ?);",
                   DBStatements.TABLE_COMPANY_NAME,
                   DBStatements.KEY_ID,
                   DBStatements.KEY_NAME,
                   DBStatements.KEY_DESCRIPTION,
                   DBStatements.KEY_WEBSITE_LINK,
                   DBStatements.KEY_ADDRESS];
            
            [RHCareerFairLayout.database executeUpdate:sql
                                  withErrorAndBindings:&error,
             company.id,
             company.company_name,
             company.company_description,
             company.company_websiteLink,
             company.company_address];
            
            
            sql = [[NSString alloc] initWithFormat:@"INSERT INTO %@ (%@, %@) VALUES(?, ?);",
                   DBStatements.TABLE_SELECTED_COMPANIES_NAME,
                   DBStatements.KEY_COMPANY_ID,
                   DBStatements.KEY_SELECTED];
            
            [RHCareerFairLayout.database executeUpdate:sql withArgumentsInArray:@[
                                                                                  company.id,
                                                                                  @true]];
        }
        
        // Insert CompanyCategories (Fail on duplicates)
        CFCompanyCategoryDict* companyCategories = data.companyCategoryDict;
        for(id key in companyCategories.companyCategoryDict){
            CFCompanyCategory* companyCategory = (CFCompanyCategory*)[companyCategories objectForKey:key];
            
            for(int i = 0; i < [companyCategory.companyCategory_categories count]; i++){
                
                sql = [[NSString alloc] initWithFormat:@"INSERT INTO %@ (%@, %@) VALUES(?, ?);",
                       DBStatements.TABLE_COMPANYCATEGORY_NAME,
                       DBStatements.KEY_COMPANY_ID,
                       DBStatements.KEY_CATEGORY_ID];
                
                [RHCareerFairLayout.database executeUpdate:sql
                                      withErrorAndBindings:&error,
                 companyCategory.companyCategory_companyId,
                 (NSNumber*)companyCategory.companyCategory_categories[i]];
            }
        }
        
        // Insert TableMappings (Fail on duplicates)
        CFTableMappingArray* tableMappings = data.tableMappingArray;
        for(id item in tableMappings.tableMappingArray){
            CFTableMapping* tableMapping = (CFTableMapping*)item;
            
            sql = [[NSString alloc] initWithFormat:@"INSERT INTO %@ (%@, %@, %@) VALUES(?, ?, ?);",
                   DBStatements.TABLE_TABLEMAPPING_NAME,
                   DBStatements.KEY_ID,
                   DBStatements.KEY_COMPANY_ID,
                   DBStatements.KEY_SIZE];
            
            [RHCareerFairLayout.database executeUpdate:sql
                                  withErrorAndBindings:&error,
             tableMapping.id,
             tableMapping.tableMapping_companyId,
             tableMapping.tableMapping_size];
        }
        
        // Insert Term (Fail on duplicate)
        CFTerm* term = data.term;
        
        sql = [[NSString alloc] initWithFormat:@"INSERT INTO %@ (%@, %@, %@, %@, %@, %@, %@, %@) VALUES(?, ?, ?, ?, ?, ?, ?, ?);",
               DBStatements.TABLE_TERM_NAME,
               DBStatements.KEY_YEAR,
               DBStatements.KEY_QUARTER,
               DBStatements.KEY_LAYOUT_SECTION1,
               DBStatements.KEY_LAYOUT_SECTION2,
               DBStatements.KEY_LAYOUT_SECTION2_PATHWIDTH,
               DBStatements.KEY_LAYOUT_SECTION2_ROWS,
               DBStatements.KEY_LAYOUT_SECTION3,
               DBStatements.KEY_LAST_UPDATE_TIME];
        
        [RHCareerFairLayout.database executeUpdate:sql
                              withErrorAndBindings:&error,
         term.term_year,
         term.term_quarter,
         term.term_layout_Section1,
         term.term_layout_Section2,
         term.term_layout_Section2_PathWidth,
         term.term_layout_Section2_Rows,
         term.term_layout_Section3,
         term.term_lastUpdateTime];
        
        [RHCareerFairLayout.database commit];
        [RHCareerFairLayout.database close];
    }
}

@end
