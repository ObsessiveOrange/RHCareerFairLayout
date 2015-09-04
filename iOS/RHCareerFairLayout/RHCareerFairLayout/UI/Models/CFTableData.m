//
//  CFTableData.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/10/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFTableData.h"

@implementation CFTableData

-(id) initWithTableMapping:(CFTableMapping*)tableMapping withRectangle: (CFRectangle*) rectangle selected: (bool) selected;{
    
    self = [super init];
    
    if(self){
        
        self.tableMapping = tableMapping;
        self.rectangle = rectangle;
        self.selected = selected;
        
        return self;
    }
    return nil;
}

@end
