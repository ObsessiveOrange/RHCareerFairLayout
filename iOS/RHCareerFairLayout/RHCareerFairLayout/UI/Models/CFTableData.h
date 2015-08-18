//
//  CFTableData.h
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/10/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "CFTableMapping.h"
#import "CFRectangle.h"

@interface CFTableData : NSObject

@property (nonatomic, strong) CFTableMapping* tableMapping;
@property (nonatomic, strong) CFRectangle* rectangle;
@property (nonatomic) bool selected;

-(id) initWithTableMapping:(CFTableMapping*)tableMapping withRectangle: (CFRectangle*) rectangle selected: (bool) selected;

@end
