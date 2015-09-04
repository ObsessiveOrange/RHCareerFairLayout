//
//  DataWrapper.h
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CFCategoryDict.h"
#import "CFCompanyCategoryDict.h"
#import "CFCompanyDict.h"
#import "CFTableMappingArray.h"
#import "CFTerm.h"

@interface CFDataWrapper : NSObject

@property (strong, nonatomic) CFCategoryDict* categoryDict;
@property (strong, nonatomic) CFCompanyCategoryDict* companyCategoryDict;
@property (strong, nonatomic) CFCompanyDict* companyDict;
@property (strong, nonatomic) CFTableMappingArray* tableMappingArray;
@property (strong, nonatomic) CFTerm* term;

- (id) initWithDictionary: (NSDictionary*) data;

@end
