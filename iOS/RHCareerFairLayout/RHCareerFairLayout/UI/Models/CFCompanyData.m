//
//  CFCompanyData.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/8/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFCompanyData.h"

@implementation CFCompanyData

- (id) initWithId:(NSNumber *)id withName:(NSString *)name withDescription:(NSString *)description withWebsiteLink:(NSString *)websiteLink withAddress:(NSString *)address withTable:(NSNumber *)table selected:(bool) selected{
    
    self = [super initWithId:id withName:name withDescription:description withWebsiteLink:websiteLink withAddress:address];
    
    if(self){
        self.company_table = table;
        self.company_selected = selected;
        
        return self;
    }
    
    return nil;
    
}

@end
