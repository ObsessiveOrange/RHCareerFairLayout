//
//  TableMapping.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFTableMapping.h"

@implementation CFTableMapping

- (id) initWithId: (NSInteger) id withCompanyId: (NSNumber*) companyId withSize: (NSInteger) size{
    
    self = [super initWithId:id];
    if(self){
        
        self.tableMapping_companyId = companyId;
        self.tableMapping_size = size;
        
        return self;
    }
    return nil;
    
}

@end
