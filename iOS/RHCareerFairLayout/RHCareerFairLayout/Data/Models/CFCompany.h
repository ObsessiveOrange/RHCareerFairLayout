//
//  Company.h
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CFEntry.h"

@interface CFCompany : CFEntry

@property (nonatomic, strong) NSString* company_name;
@property (nonatomic, strong) NSString* company_description;
@property (nonatomic, strong) NSString* company_websiteLink;
@property (nonatomic, strong) NSString* company_address;

- (id) initWithId: (NSNumber*)id withName: (NSString*)name withDescription: (NSString*)description withWebsiteLink: (NSString*)websiteLink withAddress: (NSString*)address;

@end
