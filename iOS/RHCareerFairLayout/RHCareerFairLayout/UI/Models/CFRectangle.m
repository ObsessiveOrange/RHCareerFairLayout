//
//  CFRectangle.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/10/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFRectangle.h"

@implementation CFRectangle

-(id) initWithRect: (CGRect) rect withLineColor: (UIColor*) lineColor withFillColor: (UIColor*) fillColor withText: (NSString*) text tappable: (bool) tappable{
    self = [super init];
    
    if(self){
        
        self.rect = rect;
        self.lineColor = lineColor;
        self.fillColor = fillColor;
        self.text = text;
        self.tappable = tappable;
        
        return self;
    }
    return nil;
}

@end
