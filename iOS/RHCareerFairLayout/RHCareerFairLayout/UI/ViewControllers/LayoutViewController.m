//
//  LayoutViewController.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/4/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "RHCareerFairLayout-Swift.h"
#import "LayoutViewController.h"
#import "CanvasView.h"
#import "TabBarController.h"
#import "AppDelegate.h"
#import "DetailViewController.h"
#import <FontAwesomeKit/FontAwesomeKit.h>

@interface LayoutViewController ()
@property (weak, nonatomic) IBOutlet CanvasView *canvasView;

@property (weak, nonatomic) IBOutlet UIBarButtonItem *moreMenuIcon;

@end

@implementation LayoutViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [self.navigationController.navigationBar setBarStyle:UIBarStyleBlack];
    self.navigationController.navigationBar.barTintColor = RHCareerFairLayout.color_primary;
    self.navigationController.navigationBar.tintColor = [UIColor whiteColor];
    self.navigationItem.title = @"Layout";
    
    FAKIonIcons *menu = [FAKIonIcons androidMoreIconWithSize:17];
    self.moreMenuIcon.title = @"";
    self.moreMenuIcon.image = [menu imageWithSize:CGSizeMake(30, 30)];
    
    self.canvasView.layoutViewController = self;
}

- (void)viewDidAppear:(BOOL)animated{
    
    NSNumber* highlightCompany = ((AppDelegate*) [[UIApplication sharedApplication] delegate]).hightlightTableId;
    if(highlightCompany){
        ((AppDelegate*) [[UIApplication sharedApplication] delegate]).hightlightTableId = nil;
        
        [self performSelector:@selector(flashCompany:) withObject:highlightCompany afterDelay:0.0];
        [self performSelector:@selector(flashCompany:) withObject:nil afterDelay:1.0];
        [self performSelector:@selector(flashCompany:) withObject:highlightCompany afterDelay:2.0];
        [self performSelector:@selector(flashCompany:) withObject:nil afterDelay:3.0];
        [self performSelector:@selector(flashCompany:) withObject:highlightCompany afterDelay:4.0];
        [self performSelector:@selector(flashCompany:) withObject:nil afterDelay:5.0];
    }
    
    [self.canvasView setupView];
    [self.canvasView setNeedsDisplay];
}

- (void) flashCompany:(NSNumber *)company{
    
    self.canvasView.highlightCompany = company;
    [self.canvasView setNeedsDisplay];
}

- (void)viewDidLayoutSubviews{
    [super viewDidLayoutSubviews];
    
    [self.canvasView setupView];
    [self.canvasView setNeedsDisplay];
}

- (IBAction)showMenuActionSheet:(id)sender {
    
    UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:@"Layout Options Menu"
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                               destructiveButtonTitle:nil
                                                    otherButtonTitles:@"Refresh Data", @"About", nil];
    
    [actionSheet showInView:self.view];
    
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    UIViewController* parent;
    
    switch(buttonIndex){
        case 0: //refresh data
            
            parent = [self parentViewController];
            
            while(![parent isKindOfClass:[TabBarController class]]){
                parent = [parent parentViewController];
            }
            
            [parent performSegueWithIdentifier:@"ReloadSegue" sender:self];
            
            break;
            break;
        case 1: //about
            [[[UIAlertView alloc] initWithTitle:@"About"
                                        message:RHCareerFairLayout.aboutString
                                       delegate:self
                              cancelButtonTitle:@"OK"
                              otherButtonTitles:nil] show];
            break;
    }
    
}


#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if([segue.identifier isEqualToString:RHCareerFairLayout.companyDetailSegueIdentifier]){
        DetailViewController* detailView = (DetailViewController*)((UINavigationController*)segue.destinationViewController).topViewController;
        detailView.companyData = self.selectedCompany;
        self.selectedCompany = nil;
    }
    
}

@end
