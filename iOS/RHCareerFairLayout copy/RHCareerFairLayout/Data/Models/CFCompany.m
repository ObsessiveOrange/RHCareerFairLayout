//
//  Company.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFCompany.h"

@implementation CFCompany

- (id) initWithId: (NSNumber*) id withName: (NSString*)name withDescription: (NSString*)description withWebsiteLink: (NSString*)websiteLink withAddress: (NSString*) address{
    
    self = [super initWithId:id];
    if(self){
        
        self.company_name = name;
        self.company_description = description;
        self.company_websiteLink = websiteLink;
        self.company_address = address;
        
        return self;
    }
    return nil;
    
}

@end
