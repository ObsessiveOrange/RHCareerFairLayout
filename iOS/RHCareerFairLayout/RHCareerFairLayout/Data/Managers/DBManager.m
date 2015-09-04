//
//  DBManager.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/6/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "AppDelegate.h"
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
#import "CFCompanyData.h"
#import "CFCategoryData.h"
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
             @(category.id),
             category.category_name,
             category.category_type];
            
            
            sql = [[NSString alloc] initWithFormat:@"INSERT OR IGNORE INTO %@ (%@, %@) VALUES(?, ?);",
                   DBStatements.TABLE_SELECTED_CATEGORIES_NAME,
                   DBStatements.KEY_CATEGORY_ID,
                   DBStatements.KEY_SELECTED];
            
            [RHCareerFairLayout.database executeUpdate:sql withArgumentsInArray:@[
                                                                                  @(category.id),
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
             @(company.id),
             company.company_name,
             company.company_description,
             company.company_websiteLink,
             company.company_address];
            
            
            sql = [[NSString alloc] initWithFormat:@"INSERT INTO %@ (%@, %@) VALUES(?, ?);",
                   DBStatements.TABLE_SELECTED_COMPANIES_NAME,
                   DBStatements.KEY_COMPANY_ID,
                   DBStatements.KEY_SELECTED];
            
            [RHCareerFairLayout.database executeUpdate:sql withArgumentsInArray:@[
                                                                                  @(company.id),
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
                 @(companyCategory.companyCategory_companyId),
                 companyCategory.companyCategory_categories[i]];
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
             @(tableMapping.id),
             tableMapping.tableMapping_companyId,
             @(tableMapping.tableMapping_size)];
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
         @(term.term_layout_Section1),
         @(term.term_layout_Section2),
         @(term.term_layout_Section2_PathWidth),
         @(term.term_layout_Section2_Rows),
         @(term.term_layout_Section3),
         @([term.term_lastUpdateTime timeIntervalSince1970])];
        
        [RHCareerFairLayout.database commit];
        [RHCareerFairLayout.database close];
        
        [self updateFilteredCompaniesWithSelected:true];
    }
}

+ (NSArray*) getFilteredCompanies{
    if([RHCareerFairLayout.database open]){
        
        NSString* tables;
        
        tables = [[NSString alloc] initWithFormat:DBStatements.JOIN_STATEMENT,
                  DBStatements.TABLE_COMPANY_NAME,
                  DBStatements.VIEW_FILTERED_COMPANIES_NAME,
                  DBStatements.TABLE_COMPANY_NAME,
                  DBStatements.KEY_ID,
                  DBStatements.VIEW_FILTERED_COMPANIES_NAME,
                  DBStatements.KEY_COMPANY_ID];
        tables = [[NSString alloc] initWithFormat:DBStatements.JOIN_STATEMENT,
                  tables,
                  DBStatements.TABLE_TABLEMAPPING_NAME,
                  DBStatements.TABLE_COMPANY_NAME,
                  DBStatements.KEY_ID,
                  DBStatements.TABLE_TABLEMAPPING_NAME,
                  DBStatements.KEY_COMPANY_ID];
        tables = [[NSString alloc] initWithFormat:DBStatements.JOIN_STATEMENT,
                  tables,
                  DBStatements.TABLE_SELECTED_COMPANIES_NAME,
                  DBStatements.TABLE_COMPANY_NAME,
                  DBStatements.KEY_ID,
                  DBStatements.TABLE_SELECTED_COMPANIES_NAME,
                  DBStatements.KEY_COMPANY_ID];
        
        NSArray* projection = @[
                                [self getColumnNameFromTable:DBStatements.TABLE_COMPANY_NAME withName:DBStatements.KEY_ID asAlias:DBStatements.KEY_ID],
                                DBStatements.KEY_NAME,
                                DBStatements.KEY_DESCRIPTION,
                                DBStatements.KEY_WEBSITE_LINK,
                                DBStatements.KEY_ADDRESS,
                                [self getColumnNameFromTable:DBStatements.TABLE_TABLEMAPPING_NAME withName:DBStatements.KEY_ID asAlias:DBStatements.KEY_TABLE],
                                DBStatements.KEY_SELECTED
                                ];
        
        NSString* criteria = [[NSString alloc] initWithFormat:@"%@.%@ LIKE ?", DBStatements.TABLE_COMPANY_NAME, DBStatements.KEY_NAME];
        
        NSString* order = [[NSString alloc] initWithFormat:@"%@.%@ %@", DBStatements.TABLE_COMPANY_NAME, DBStatements.KEY_NAME, @"COLLATE NOCASE ASC"];
        
        NSString* sql = [[NSString alloc] initWithFormat:@"SELECT %@ FROM %@ WHERE %@ ORDER BY %@",
                         [self getProjectionStatementWithColumns:projection],
                         tables,
                         criteria,
                         order
                         ];
        
        FMResultSet* results = [RHCareerFairLayout.database executeQuery:sql,
                                [[NSString alloc] initWithFormat:@"%%%@%%", [[NSUserDefaults standardUserDefaults] stringForKey:@"searchText"]]];
        
        NSMutableArray* resultsArray = [[NSMutableArray alloc] init];
        
        while([results next]){
            CFCompanyData* companyData = [[CFCompanyData alloc]
                                          initWithId:[results intForColumn:DBStatements.KEY_ID]
                                          withName:[results stringForColumn:DBStatements.KEY_NAME]
                                          withDescription:[results stringForColumn:DBStatements.KEY_DESCRIPTION]
                                          withWebsiteLink:[results stringForColumn:DBStatements.KEY_WEBSITE_LINK]
                                          withAddress:[results stringForColumn:DBStatements.KEY_ADDRESS]
                                          withTable:[results intForColumn:DBStatements.KEY_TABLE]
                                          selected:[results boolForColumn:DBStatements.KEY_SELECTED]];
            
            [resultsArray addObject:companyData];
        }
        
        [RHCareerFairLayout.database close];
        
        return resultsArray;
    }
    
    return [[NSArray alloc] init];
}

+ (CFCompanyData*) getCompanyWithId: (NSInteger) companyId{
    if([RHCareerFairLayout.database open]){
        
        NSString* tables;
        
        tables = [[NSString alloc] initWithFormat:DBStatements.JOIN_STATEMENT,
                  DBStatements.TABLE_COMPANY_NAME,
                  DBStatements.VIEW_FILTERED_COMPANIES_NAME,
                  DBStatements.TABLE_COMPANY_NAME,
                  DBStatements.KEY_ID,
                  DBStatements.VIEW_FILTERED_COMPANIES_NAME,
                  DBStatements.KEY_COMPANY_ID];
        tables = [[NSString alloc] initWithFormat:DBStatements.JOIN_STATEMENT,
                  tables,
                  DBStatements.TABLE_TABLEMAPPING_NAME,
                  DBStatements.TABLE_COMPANY_NAME,
                  DBStatements.KEY_ID,
                  DBStatements.TABLE_TABLEMAPPING_NAME,
                  DBStatements.KEY_COMPANY_ID];
        tables = [[NSString alloc] initWithFormat:DBStatements.JOIN_STATEMENT,
                  tables,
                  DBStatements.TABLE_SELECTED_COMPANIES_NAME,
                  DBStatements.TABLE_COMPANY_NAME,
                  DBStatements.KEY_ID,
                  DBStatements.TABLE_SELECTED_COMPANIES_NAME,
                  DBStatements.KEY_COMPANY_ID];
        
        NSArray* projection = @[
                                [self getColumnNameFromTable:DBStatements.TABLE_COMPANY_NAME withName:DBStatements.KEY_ID asAlias:DBStatements.KEY_ID],
                                DBStatements.KEY_NAME,
                                DBStatements.KEY_DESCRIPTION,
                                DBStatements.KEY_WEBSITE_LINK,
                                DBStatements.KEY_ADDRESS,
                                [self getColumnNameFromTable:DBStatements.TABLE_TABLEMAPPING_NAME withName:DBStatements.KEY_ID asAlias:DBStatements.KEY_TABLE],
                                DBStatements.KEY_SELECTED
                                ];
        
        NSString* criteria = [[NSString alloc] initWithFormat:@"%@.%@ = ?", DBStatements.TABLE_COMPANY_NAME, DBStatements.KEY_ID];
        
        NSString* sql = [[NSString alloc] initWithFormat:@"SELECT %@ FROM %@ WHERE %@",
                         [self getProjectionStatementWithColumns:projection],
                         tables,
                         criteria
                         ];
        
        FMResultSet* results = [RHCareerFairLayout.database executeQuery:sql,
                                @(companyId)];
        
        CFCompanyData* companyData = nil;
        
        if([results next]){
            companyData = [[CFCompanyData alloc]
                           initWithId:[results intForColumn:DBStatements.KEY_ID]
                           withName:[results stringForColumn:DBStatements.KEY_NAME]
                           withDescription:[results stringForColumn:DBStatements.KEY_DESCRIPTION]
                           withWebsiteLink:[results stringForColumn:DBStatements.KEY_WEBSITE_LINK]
                           withAddress:[results stringForColumn:DBStatements.KEY_ADDRESS]
                           withTable:[results intForColumn:DBStatements.KEY_TABLE]
                           selected:[results boolForColumn:DBStatements.KEY_SELECTED]];
        }
        [RHCareerFairLayout.database close];
        
        return companyData;
    }
    
    return nil;
}

+ (void) setCompany: (NSInteger) companyId selected:(bool)selected{
    if([RHCareerFairLayout.database open]){
        
        // Update selected (Replace on duplicate)
        
        NSString* sql = [[NSString alloc] initWithFormat:@"INSERT OR REPLACE INTO %@ (%@, %@) VALUES(?, ?);",
                         DBStatements.TABLE_SELECTED_COMPANIES_NAME,
                         DBStatements.KEY_COMPANY_ID,
                         DBStatements.KEY_SELECTED];
        
        NSError* error;
        
        [RHCareerFairLayout.database executeUpdate:sql
                              withErrorAndBindings:&error,
         @(companyId),
         @(selected)];
        
        [RHCareerFairLayout.database close];
    }
    
}

+ (void) updateFilteredCompaniesWithSelected: (bool)selected{
    
    NSArray* filteredCompanies = [self getFilteredCompanies];
    
    [RHCareerFairLayout.database open];
    [RHCareerFairLayout.database beginTransaction];
    
    [RHCareerFairLayout.database executeUpdate:[[NSString alloc]
                                                initWithFormat:@"UPDATE %@ SET %@ = 0",
                                                DBStatements.TABLE_SELECTED_COMPANIES_NAME,
                                                DBStatements.KEY_SELECTED]];
    
    NSString* updateFilteredCompanies = [[NSString alloc] initWithFormat:@"INSERT OR REPLACE INTO %@ (%@, %@) VALUES(?, ?);",
                                         DBStatements.TABLE_SELECTED_COMPANIES_NAME,
                                         DBStatements.KEY_COMPANY_ID,
                                         DBStatements.KEY_SELECTED];
    
    for(int i = 0; i < [filteredCompanies count]; i++){
        
        CFCompanyData* companyData = filteredCompanies[i];
        
        NSError* error;
        
        [RHCareerFairLayout.database executeUpdate:updateFilteredCompanies withErrorAndBindings:&error,
         @(companyData.id),
         @(selected)];
        
    }
    
    [RHCareerFairLayout.database commit];
    [RHCareerFairLayout.database close];
}

+ (NSArray*) getCategories{
    if([RHCareerFairLayout.database open]){
        
        NSString* tables = [[NSString alloc] initWithFormat:DBStatements.JOIN_STATEMENT,
                            DBStatements.TABLE_CATEGORY_NAME,
                            DBStatements.TABLE_SELECTED_CATEGORIES_NAME,
                            DBStatements.TABLE_CATEGORY_NAME,
                            DBStatements.KEY_ID,
                            DBStatements.TABLE_SELECTED_CATEGORIES_NAME,
                            DBStatements.KEY_CATEGORY_ID];
        
        NSArray* projection = @[
                                DBStatements.KEY_ID,
                                DBStatements.KEY_NAME,
                                DBStatements.KEY_TYPE,
                                DBStatements.KEY_SELECTED
                                ];
        
        NSString* order = [[NSString alloc] initWithFormat:@"%@ COLLATE NOCASE ASC, %@ COLLATE NOCASE ASC", DBStatements.KEY_TYPE, DBStatements.KEY_NAME];
        
        NSString* sql = [[NSString alloc] initWithFormat:@"SELECT %@ FROM %@ ORDER BY %@",
                         [self getProjectionStatementWithColumns:projection],
                         tables,
                         order
                         ];
        
        FMResultSet* results = [RHCareerFairLayout.database executeQuery:sql];
        
        NSMutableArray* resultsArray = [[NSMutableArray alloc] init];
        
        while([results next]){
            CFCategoryData* categoryData = [[CFCategoryData alloc]
                                            initWithId:[results intForColumn:DBStatements.KEY_ID]
                                            withName:[results stringForColumn:DBStatements.KEY_NAME]
                                            withType:[results stringForColumn:DBStatements.KEY_TYPE]
                                            selected:[results boolForColumn:DBStatements.KEY_SELECTED]];
            
            [resultsArray addObject:categoryData];
        }
        
        [RHCareerFairLayout.database close];
        return resultsArray;
    }
    
    return [[NSArray alloc] init];
}

+ (NSDictionary*) getCategoriesForCompany: (NSInteger) companyId{
    if([RHCareerFairLayout.database open]){
        
        NSMutableDictionary* categories = [[NSMutableDictionary alloc] init];
        // Update selected (Replace on duplicate)
        
        NSString* tables = [[NSString alloc] initWithFormat:DBStatements.JOIN_STATEMENT,
                            DBStatements.TABLE_COMPANYCATEGORY_NAME,
                            DBStatements.TABLE_CATEGORY_NAME,
                            DBStatements.TABLE_COMPANYCATEGORY_NAME,
                            DBStatements.KEY_CATEGORY_ID,
                            DBStatements.TABLE_CATEGORY_NAME,
                            DBStatements.KEY_ID];
        
        
        NSString* sql = [[NSString alloc] initWithFormat:@"SELECT %@ FROM %@ WHERE %@ = ? AND %@ = ?;",
                         DBStatements.KEY_NAME,
                         tables,
                         DBStatements.KEY_COMPANY_ID,
                         DBStatements.KEY_TYPE];
        
        FMResultSet* majors = [RHCareerFairLayout.database executeQuery:sql,
                               @(companyId),
                               @"Major"];
        
        FMResultSet* positionTypes = [RHCareerFairLayout.database executeQuery:sql,
                                      @(companyId),
                                      @"Position Type"];
        
        FMResultSet* workAuthorizations = [RHCareerFairLayout.database executeQuery:sql,
                                           @(companyId),
                                           @"Work Authorization"];
        
        NSMutableArray* majorsArray = [[NSMutableArray alloc] init];
        NSMutableArray* positionTypesArray = [[NSMutableArray alloc] init];
        NSMutableArray* workAuthorizationsArray = [[NSMutableArray alloc] init];
        
        while([majors next]){
            [majorsArray addObject:[majors stringForColumn:DBStatements.KEY_NAME]];
        }
        
        while([positionTypes next]){
            [positionTypesArray addObject:[positionTypes stringForColumn:DBStatements.KEY_NAME]];
        }
        
        while([workAuthorizations next]){
            [workAuthorizationsArray addObject:[workAuthorizations stringForColumn:DBStatements.KEY_NAME]];
        }
        
        [categories setObject:majorsArray forKey:@"Majors"];
        [categories setObject:positionTypesArray forKey:@"Position Types"];
        [categories setObject:workAuthorizationsArray forKey:@"Work Authorizations"];
        
        [RHCareerFairLayout.database close];
        
        return categories;
    }
    return nil;
    
}

+ (void) setCategory: (NSInteger) categoryId selected:(bool)selected{
    if([RHCareerFairLayout.database open]){
        // Update selected (Replace on duplicate)
        
        NSString* sql = [[NSString alloc] initWithFormat:@"INSERT OR REPLACE INTO %@ (%@, %@) VALUES(?, ?);",
                         DBStatements.TABLE_SELECTED_CATEGORIES_NAME,
                         DBStatements.KEY_CATEGORY_ID,
                         DBStatements.KEY_SELECTED];
        
        NSError* error;
        
        [RHCareerFairLayout.database executeUpdate:sql
                              withErrorAndBindings:&error,
         @(categoryId),
         @(selected)];
        
        
        [RHCareerFairLayout.database close];
        
        [self updateFilteredCompaniesWithSelected: true];
    }
}

+ (CFTableDataMap*) getTables{
    if([RHCareerFairLayout.database open]){
        // Update selected (Replace on duplicate)
        
        NSString* tables = [[NSString alloc] initWithFormat:DBStatements.LEFT_OUTER_JOIN_STATEMENT,
                            DBStatements.TABLE_TABLEMAPPING_NAME,
                            DBStatements.TABLE_SELECTED_COMPANIES_NAME,
                            DBStatements.TABLE_TABLEMAPPING_NAME,
                            DBStatements.KEY_COMPANY_ID,
                            DBStatements.TABLE_SELECTED_COMPANIES_NAME,
                            DBStatements.KEY_COMPANY_ID];
        
        NSString* sql = [[NSString alloc] initWithFormat:@"SELECT %@, %@, %@, %@ FROM %@;",
                         DBStatements.KEY_ID,
                         [self getColumnNameFromTable:DBStatements.TABLE_TABLEMAPPING_NAME withName:DBStatements.KEY_COMPANY_ID asAlias:DBStatements.KEY_COMPANY_ID],
                         DBStatements.KEY_SIZE,
                         DBStatements.KEY_SELECTED,
                         tables];
        
        FMResultSet* results = [RHCareerFairLayout.database executeQuery:sql];
        
        CFTableDataMap* tableDataMap = [[CFTableDataMap alloc] initWithTableData: results];
        
        [RHCareerFairLayout.database close];
        
        return tableDataMap;
    }
    
    return nil;
}

+ (CFTerm*) getTerm{
    if([RHCareerFairLayout.database open]){
        // Update selected (Replace on duplicate)
        
        NSString* sql = [[NSString alloc] initWithFormat:@"SELECT %@, %@, %@, %@, %@, %@, %@, %@ FROM %@;",
                         DBStatements.KEY_YEAR,
                         DBStatements.KEY_QUARTER,
                         DBStatements.KEY_LAYOUT_SECTION1,
                         DBStatements.KEY_LAYOUT_SECTION2,
                         DBStatements.KEY_LAYOUT_SECTION2_PATHWIDTH,
                         DBStatements.KEY_LAYOUT_SECTION2_ROWS,
                         DBStatements.KEY_LAYOUT_SECTION3,
                         DBStatements.KEY_LAST_UPDATE_TIME,
                         DBStatements.TABLE_TERM_NAME];
        
        FMResultSet* results = [RHCareerFairLayout.database executeQuery:sql];
        
        CFTerm* term;
        while([results next]){
            term = [[CFTerm alloc] initWithYear:[results stringForColumn:DBStatements.KEY_YEAR]
                                    withQuarter:[results stringForColumn:DBStatements.KEY_QUARTER]
                             withLayoutSection1:[results intForColumn:DBStatements.KEY_LAYOUT_SECTION1]
                             withLayoutSection2:[results intForColumn:DBStatements.KEY_LAYOUT_SECTION2]
                    withLayoutSection2PathWidth:[results intForColumn:DBStatements.KEY_LAYOUT_SECTION2_PATHWIDTH]
                         withLayoutSection2Rows:[results intForColumn:DBStatements.KEY_LAYOUT_SECTION2_ROWS]
                             withLayoutSection3:[results intForColumn:DBStatements.KEY_LAYOUT_SECTION3]
                             withLastUpdateTime:[results dateForColumn:DBStatements.KEY_LAST_UPDATE_TIME]];
        }
        
        [RHCareerFairLayout.database close];
        
        return term;
    }
    
    return nil;
}

+ (void) clearSelectedCategories{
    if([RHCareerFairLayout.database open]){
        // Update selected (Replace on duplicate)
        
        [RHCareerFairLayout.database executeUpdate:[[NSString alloc]
                                                    initWithFormat:@"UPDATE %@ SET %@ = 0",
                                                    DBStatements.TABLE_SELECTED_CATEGORIES_NAME,
                                                    DBStatements.KEY_SELECTED]];
        
        
        [RHCareerFairLayout.database close];
        
        [self updateFilteredCompaniesWithSelected: true];
    }
}

+ (NSString*) getColumnNameFromTable: (NSString*)table withName: (NSString*)name asAlias: (NSString*) alias{
    
    if(alias){
        return [[NSString alloc] initWithFormat:@"%@.%@ AS %@", table, name, alias];
    }
    
    return [[NSString alloc] initWithFormat:@"%@.%@", table, name];
    
}

+ (NSString*) getProjectionStatementWithColumns: (NSArray*) columns{
    
    NSString* projectionStmt = @"";
    
    for(int i = 0; i < [columns count]; i++){
        
        if([projectionStmt isEqualToString:@""]){
            projectionStmt = columns[i];
        }
        else{
            projectionStmt = [[NSString alloc] initWithFormat:@"%@, %@", projectionStmt, columns[i]];
        }
        
    }
    
    return projectionStmt;
    
}

@end
