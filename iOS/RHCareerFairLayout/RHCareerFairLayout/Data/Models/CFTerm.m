//
//  Term.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFTerm.h"

@implementation CFTerm

- (id) initWithDictionary: (NSDictionary*) data{
    
    self = [super init];
    if(self){
        
        self.term_year = (NSString*)[data objectForKey:@"year"];
        self.term_quarter = (NSString*)[data objectForKey:@"quarter"];
        self.term_layout_Section1 = [((NSNumber*)[data objectForKey:@"layout_Section1"]) intValue];
        self.term_layout_Section2 = [((NSNumber*)[data objectForKey:@"layout_Section2"]) intValue];
        self.term_layout_Section2_PathWidth = [((NSNumber*)[data objectForKey:@"layout_Section2_PathWidth"]) intValue];
        self.term_layout_Section2_Rows = [((NSNumber*)[data objectForKey:@"layout_Section2_Rows"]) intValue];
        self.term_layout_Section3 = [((NSNumber*)[data objectForKey:@"layout_Section3"]) intValue];
        self.term_lastUpdateTime = [NSDate date];
        
        return self;
    }
    return nil;
    
}

- (id) initWithYear: (NSString*) year withQuarter: (NSString*) quarter withLayoutSection1: (NSInteger) layout_Section1 withLayoutSection2: (NSInteger) layout_Section2 withLayoutSection2PathWidth: (NSInteger) layout_Section2_PathWidth withLayoutSection2Rows: (NSInteger) layout_Section2_Rows withLayoutSection3: (NSInteger) layout_Section3 withLastUpdateTime: (NSDate*) lastUpdateTime{
    
    self = [super init];
    if(self){
        
        self.term_year = year;
        self.term_quarter = quarter;
        self.term_layout_Section1 = layout_Section1;
        self.term_layout_Section2 = layout_Section2;
        self.term_layout_Section2_PathWidth = layout_Section2_PathWidth;
        self.term_layout_Section2_Rows = layout_Section2_Rows;
        self.term_layout_Section3 = layout_Section3;
        self.term_lastUpdateTime = lastUpdateTime;
        
        return self;
    }
    return nil;
    
}

@end
