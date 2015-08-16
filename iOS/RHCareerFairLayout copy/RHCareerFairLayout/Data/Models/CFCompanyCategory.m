//
//  CompanyCategory.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFCompanyCategory.h"

@implementation CFCompanyCategory

- (id) initWithCompanyId: (NSNumber*) companyId withCategories: (NSArray*) categories{
    
    self = [super init];
    if(self){
        
        self.companyCategory_companyId = companyId;
        self.companyCategory_categories = categories;
        
        return self;
    }
    return nil;
    
}

@end
