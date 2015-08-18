//
//  CanvasView.h
//  DrawingCanvas
//
//  Created by Benedict Seng Sum Wong on 4/24/15.
//  Copyright (c) 2015 Rose-Hulman. All rights reserved.
//

#import "LayoutViewController.h"
#import <UIKit/UIKit.h>

@interface CanvasView : UIView

- (void) setupView;

@property (weak, nonatomic) NSNumber *highlightCompany;
@property (weak, nonatomic) LayoutViewController* layoutViewController;

@end
