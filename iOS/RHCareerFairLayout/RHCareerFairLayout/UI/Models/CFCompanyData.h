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

@property (nonatomic) NSInteger company_table;
@property (nonatomic) bool company_selected;

- (id) initWithId:(NSInteger)id withName:(NSString *)name withDescription:(NSString *)description withWebsiteLink:(NSString *)websiteLink withAddress:(NSString *)address withTable:(NSInteger)table selected:(bool) selected;

@end
