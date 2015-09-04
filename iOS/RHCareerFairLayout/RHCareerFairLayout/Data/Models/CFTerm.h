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
@property (nonatomic) NSInteger term_layout_Section1;
@property (nonatomic) NSInteger term_layout_Section2;
@property (nonatomic) NSInteger term_layout_Section2_PathWidth;
@property (nonatomic) NSInteger term_layout_Section2_Rows;
@property (nonatomic) NSInteger term_layout_Section3;
@property (nonatomic) NSDate* term_lastUpdateTime;

- (id) initWithDictionary: (NSDictionary*) data;
- (id) initWithYear: (NSString*) year withQuarter: (NSString*) quarter withLayoutSection1: (NSInteger) layout_Section1 withLayoutSection2: (NSInteger) layout_Section2 withLayoutSection2PathWidth: (NSInteger) layout_Section2_PathWidth withLayoutSection2Rows: (NSInteger) layout_Section2_Rows withLayoutSection3: (NSInteger) layout_Section3 withLastUpdateTime: (NSDate*) lastUpdateTime;

@end
