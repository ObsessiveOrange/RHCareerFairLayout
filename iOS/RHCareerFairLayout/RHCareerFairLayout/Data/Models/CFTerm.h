//
//  Term.h
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CFEntry.h"

@interface CFTerm : CFEntry
@property (nonatomic, strong) NSString* term_year;
@property (nonatomic, strong) NSString* term_quarter;
@property (nonatomic, strong) NSNumber* term_layout_Section1;
@property (nonatomic, strong) NSNumber* term_layout_Section2;
@property (nonatomic, strong) NSNumber* term_layout_Section2_PathWidth;
@property (nonatomic, strong) NSNumber* term_layout_Section2_Rows;
@property (nonatomic, strong) NSNumber* term_layout_Section3;
@property (nonatomic, strong) NSNumber* term_lastUpdateTime;

- (id) initWithDictionary: (NSDictionary*) data;

@end
