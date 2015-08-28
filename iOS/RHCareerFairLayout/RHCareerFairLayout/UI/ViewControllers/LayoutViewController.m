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
#import <Google/Analytics.h>

@interface LayoutViewController ()
@property (weak, nonatomic) IBOutlet CanvasView *canvasView;

@property (weak, nonatomic) IBOutlet UIBarButtonItem *moreMenuIcon;

@end

@implementation LayoutViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Set navigation bar theme
    [self.navigationController.navigationBar setBarStyle:UIBarStyleBlack];
    self.navigationController.navigationBar.barTintColor = RHCareerFairLayout.color_primary;
    self.navigationController.navigationBar.tintColor = [UIColor whiteColor];
    self.navigationItem.title = @"Layout";
    
    // Setup icon for actionSheet menu
    FAKIonIcons *menu = [FAKIonIcons androidMoreIconWithSize:20];
    self.moreMenuIcon.title = @"";
    self.moreMenuIcon.image = [menu imageWithSize:CGSizeMake(30, 30)];
    
    // Give canvasView reference, for segue when table tapped
    self.canvasView.layoutViewController = self;
}

- (void)viewWillAppear:(BOOL)animated{
    
    [self.canvasView updateView];
    
}

- (void)viewDidAppear:(BOOL)animated{
    
    // If there is a company to be highlighted, setup the future calls to flash it.
    NSNumber* highlightCompany = ((AppDelegate*) [[UIApplication sharedApplication] delegate]).hightlightTableId;
    if(highlightCompany){
        ((AppDelegate*) [[UIApplication sharedApplication] delegate]).hightlightTableId = nil;
        
        // Cancel any outstanding request first!
        [NSObject cancelPreviousPerformRequestsWithTarget:self];
        
        // Start new animation cycle
        [self performSelector:@selector(flashCompany:) withObject:highlightCompany afterDelay:0.0];
        [self performSelector:@selector(flashCompany:) withObject:nil afterDelay:1.0];
        [self performSelector:@selector(flashCompany:) withObject:highlightCompany afterDelay:2.0];
        [self performSelector:@selector(flashCompany:) withObject:nil afterDelay:3.0];
        [self performSelector:@selector(flashCompany:) withObject:highlightCompany afterDelay:4.0];
        [self performSelector:@selector(flashCompany:) withObject:nil afterDelay:5.0];
    }
    
    id<GAITracker> tracker = [[GAI sharedInstance] defaultTracker];
    [tracker set:kGAIScreenName value:@"Layout"];
    [tracker send:[[GAIDictionaryBuilder createScreenView] build]];
}

- (void) flashCompany:(NSNumber *)company{
    // Update view's data, and redraw
    self.canvasView.highlightCompany = company;
    [self.canvasView setNeedsDisplay];
}

- (void)viewDidLayoutSubviews{
    [super viewDidLayoutSubviews];
    
    //Earliest time that setup can be done - must have layout already defined.
    [self.canvasView setupView];
    [self.canvasView setNeedsDisplay];
}

- (IBAction)showMenuActionSheet:(id)sender {
    
    UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:nil
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                               destructiveButtonTitle:nil
                                                    otherButtonTitles:
                                  @"Refresh Data",
                                  @"About",
                                  nil];
    
    [actionSheet showInView:self.view];
    
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    switch(buttonIndex){
        case 0: //refresh data
            
            // Set force reload flag - no cache.
            ((AppDelegate*)[[UIApplication sharedApplication] delegate]).forceReload = true;
            
            [[TabBarController instance] performSegueWithIdentifier:@"ReloadSegue" sender:self];
            
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

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    
    // Set detail view's company data
    if([segue.identifier isEqualToString:RHCareerFairLayout.companyDetailSegueIdentifier]){
        DetailViewController* detailView = (DetailViewController*)((UINavigationController*)segue.destinationViewController).topViewController;
        detailView.companyData = self.selectedCompany;
        self.selectedCompany = nil;
    }
    
}

@end
