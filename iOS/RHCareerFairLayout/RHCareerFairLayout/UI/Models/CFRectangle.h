//
//  CFRectangle.h
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/10/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface CFRectangle : NSObject

@property (nonatomic) CGRect rect;
@property (nonatomic, strong) UIColor* lineColor;
@property (nonatomic, strong) UIColor* fillColor;
@property (nonatomic, strong) NSString* text;
@property (nonatomic) bool tappable;

-(id) initWithRect: (CGRect) rect withLineColor: (UIColor*) lineColor withFillColor: (UIColor*) fillColor withText: (NSString*) text tappable: (bool) tappable;

@end
