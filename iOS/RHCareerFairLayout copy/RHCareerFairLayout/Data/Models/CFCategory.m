//
//  Category.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFCategory.h"

@implementation CFCategory

- (id) initWithId: (NSNumber*) id withName: (NSString*) name withType: (NSString*) type {
    
    self = [super initWithId:id];
    if(self){
        
        self.category_name = name;
        self.category_type = type;
        
        return self;
    }
    return nil;
}

@end
