//
//  CanvasView.h
//  DrawingCanvas
//
//  Created by Benedict Seng Sum Wong on 4/24/15.
//  Copyright (c) 2015 Rose-Hulman. All rights reserved.
//

#import "LayoutViewController.h"
#import <UIKit/UIKit.h>

typedef NS_ENUM(NSInteger, UIOrientation) {
    UIOrientationPortrait,
    UIOrientationLandscape
};

@interface CanvasView : UIView

- (void) setupView;

@property (strong, nonatomic) volatile NSNumber *highlightCompany;
@property (weak, nonatomic) LayoutViewController* layoutViewController;

@end
