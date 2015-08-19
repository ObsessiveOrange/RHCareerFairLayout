//
//  CFCategoryData.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/9/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFCategoryData.h"

@implementation CFCategoryData

-(id) initWithId: (NSInteger) id withName: (NSString*) name withType: (NSString*) type selected: (bool) selected{
    self = [super initWithId:id withName:name withType:type];
    
    if(self){
        self.category_selected = selected;
        
        return self;
    }
    return nil;
}

@end
