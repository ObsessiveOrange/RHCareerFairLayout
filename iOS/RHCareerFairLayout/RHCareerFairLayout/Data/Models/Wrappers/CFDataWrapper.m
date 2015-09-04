//
//  DataWrapper.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFDataWrapper.h"

@implementation CFDataWrapper

- (id) initWithDictionary:(NSDictionary *) data{
    
    self = [super init];
    
    if(self){
        
        NSDictionary* companyDict = (NSDictionary*)[data objectForKey:@"companyMap"];
        NSDictionary* companyCategoryDict = (NSDictionary*)[data objectForKey:@"companyCategoryMap"];
        NSDictionary* categoryDict = (NSDictionary*)[data objectForKey:@"categoryMap"];
        NSArray* tableMappingArray = (NSArray*)[data objectForKey:@"tableMappingList"];
        NSDictionary* term = (NSDictionary*)[data objectForKey:@"term"];
        
        self.companyDict = [[CFCompanyDict alloc] initWithDictionary:companyDict];
        self.companyCategoryDict = [[CFCompanyCategoryDict alloc] initWithDictionary:companyCategoryDict];
        self.categoryDict = [[CFCategoryDict alloc] initWithDictionary:categoryDict];
        self.tableMappingArray = [[CFTableMappingArray alloc] initWithArray:tableMappingArray];
        self.term = [[CFTerm alloc] initWithDictionary:term];
        
        return self;
    }
    
    return nil;
}

@end
