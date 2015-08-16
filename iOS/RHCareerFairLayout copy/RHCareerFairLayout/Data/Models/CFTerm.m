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
        self.term_layout_Section1 = [[NSNumber alloc] initWithInteger: (int)[data objectForKey:@"ilayout_Section1"]];
        self.term_layout_Section2 = [[NSNumber alloc] initWithInteger: (int)[data objectForKey:@"ilayout_Section2"]];
        self.term_layout_Section2_PathWidth = [[NSNumber alloc] initWithInteger: (int)[data objectForKey:@"ilayout_Section2_PathWidth"]];
        self.term_layout_Section2_Rows = [[NSNumber alloc] initWithInteger: (int)[data objectForKey:@"ilayout_Section2_Rows"]];
        self.term_layout_Section3 = [[NSNumber alloc] initWithInteger: (int)[data objectForKey:@"ilayout_Section3"]];
        
        return self;
    }
    return nil;    
    
}

@end
