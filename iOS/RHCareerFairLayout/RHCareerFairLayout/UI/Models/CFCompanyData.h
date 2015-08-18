//
//  CFCompanyData.h
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/8/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CFCompany.h"

@interface CFCompanyData : CFCompany

@property (strong, nonatomic) NSNumber* company_table;
@property (nonatomic) bool company_selected;

- (id) initWithId:(NSNumber *)id withName:(NSString *)name withDescription:(NSString *)description withWebsiteLink:(NSString *)websiteLink withAddress:(NSString *)address withTable:(NSNumber *)table selected:(bool) selected;

@end
