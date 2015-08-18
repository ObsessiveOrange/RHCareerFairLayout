//
//  ReloadSegue.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/9/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "ReloadSegue.h"

@implementation ReloadSegue

-(void)perform {
    UIViewController *sourceViewController = (UIViewController*)[self sourceViewController];
    UIViewController *destinationController = (UIViewController*)[self destinationViewController];
    UINavigationController *navigationController = sourceViewController.navigationController;
    // Pop to root view controller (not animated) before pushing
    [navigationController popToRootViewControllerAnimated:NO];
    [navigationController pushViewController:destinationController animated:YES];
    
    
//    // Get a changeable copy of the stack
//    NSMutableArray *controllerStack = [NSMutableArray arrayWithArray:navigationController.viewControllers];
//    // Replace the source controller with the destination controller, wherever the source may be
//    [controllerStack replaceObjectAtIndex:[controllerStack indexOfObject:sourceViewController] withObject:destinationController];
//    
//    // Assign the updated stack with animation
//    [navigationController setViewControllers:controllerStack animated:YES];
}

@end
