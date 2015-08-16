//
//  Entry.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFEntry.h"

@implementation CFEntry

- (id) initWithId: (NSNumber*) id{
    
    self = [super init];
    if(self){
        
        self.id = id;
        
        return self;
    }
    return nil;
    
}

@end
