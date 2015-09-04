//
//  DetailViewController.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/9/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "RHCareerFairLayout-Swift.h"
#import "DetailViewController.h"
#import "CompaniesViewController.h"
#import "DBManager.h"
#import "AppDelegate.h"
#import <FontAwesomeKit/FontAwesomeKit.h>
#import <Google/Analytics.h>

@interface DetailViewController ()

@property (strong, nonatomic) IBOutlet UIBarButtonItem *backBtn;
@property (strong, nonatomic) IBOutlet UIBarButtonItem *viewOnMapBtn;

@property (strong, nonatomic) IBOutlet UILabel *companyNameLabel;
@property (strong, nonatomic) IBOutlet UITextView *websiteLinkTextView;
@property (strong, nonatomic) IBOutlet NSLayoutConstraint *websiteLinkTextViewHeight;
@property (strong, nonatomic) IBOutlet UILabel *descriptionHeaderLabel;
@property (strong, nonatomic) IBOutlet UILabel *descriptionBodyLabel;
@property (strong, nonatomic) IBOutlet UILabel *majorsHeaderLabel;
@property (strong, nonatomic) IBOutlet UILabel *majorsBodyLabel;
@property (strong, nonatomic) IBOutlet UILabel *positionTypesHeaderLabel;
@property (strong, nonatomic) IBOutlet UILabel *positionTypesBodyLabel;
@property (strong, nonatomic) IBOutlet UILabel *workAuthorizationsHeaderLabel;
@property (strong, nonatomic) IBOutlet UILabel *workAuthorizationsBodyLabel;
@property (strong, nonatomic) IBOutlet UILabel *addressHeaderLabel;
@property (strong, nonatomic) IBOutlet UILabel *addressBodyLabel;

@end

@implementation DetailViewController


- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self.navigationController.navigationBar setBarStyle:UIBarStyleBlack];
    self.navigationController.navigationBar.barTintColor = RHCareerFairLayout.color_primary;
    self.navigationController.navigationBar.tintColor = [UIColor whiteColor];
    self.navigationItem.title = @"CompanyDetail";
    
    NSDictionary *categories = [DBManager getCategoriesForCompany:self.companyData.id];
    
    if(self.companyData.company_name != nil && ![self.companyData.company_name isEqualToString:@""]){
        self.companyNameLabel.text = self.companyData.company_name;
    }
    else{
        [self.companyNameLabel addConstraint:[NSLayoutConstraint constraintWithItem:self.companyNameLabel attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:0]];
    }
    
    if(self.companyData.company_websiteLink != nil && ![self.companyData.company_websiteLink isEqualToString:@""]){
        self.websiteLinkTextView.text = self.companyData.company_websiteLink;
        self.websiteLinkTextViewHeight.constant = [self.websiteLinkTextView sizeThatFits:CGSizeMake(self.websiteLinkTextView.frame.size.width, CGFLOAT_MAX)].height;
    }
    else{
        self.websiteLinkTextViewHeight.constant = 0;
    }
    
    if(self.companyData.company_description != nil && ![self.companyData.company_description isEqualToString:@""]){
        self.descriptionBodyLabel.text = self.companyData.company_description;
    }
    else{
        [self.descriptionHeaderLabel addConstraint:[NSLayoutConstraint constraintWithItem:self.descriptionHeaderLabel attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:0]];
        [self.descriptionBodyLabel addConstraint:[NSLayoutConstraint constraintWithItem:self.descriptionBodyLabel attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:0]];
    }
    
    NSArray* majors = [categories objectForKey:@"Majors"];
    if(majors != nil && [majors count] != 0){
        NSString *majorsString = [majors componentsJoinedByString:@", "];
        
        self.majorsBodyLabel.text = majorsString;
    }
    else{
        [self.majorsHeaderLabel addConstraint:[NSLayoutConstraint constraintWithItem:self.majorsHeaderLabel attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:0]];
        [self.majorsBodyLabel addConstraint:[NSLayoutConstraint constraintWithItem:self.majorsBodyLabel attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:0]];
    }
    
    NSArray* positionTypes = [categories objectForKey:@"Position Types"];
    if(positionTypes != nil && [positionTypes count] != 0){
        NSString *positionTypesString = [positionTypes componentsJoinedByString:@", "];
        
        self.positionTypesBodyLabel.text = positionTypesString;
    }
    else{
        [self.positionTypesHeaderLabel addConstraint:[NSLayoutConstraint constraintWithItem:self.positionTypesHeaderLabel attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:0]];
        [self.positionTypesBodyLabel addConstraint:[NSLayoutConstraint constraintWithItem:self.positionTypesBodyLabel attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:0]];
    }
    
    NSArray* workAuthorizations = [categories objectForKey:@"Work Authorizations"];
    if(workAuthorizations != nil && [workAuthorizations count] != 0){
        NSString *workAuthorizationsString = [workAuthorizations componentsJoinedByString:@", "];
        
        self.workAuthorizationsBodyLabel.text = workAuthorizationsString;
    }
    else{
        [self.workAuthorizationsHeaderLabel addConstraint:[NSLayoutConstraint constraintWithItem:self.workAuthorizationsHeaderLabel attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:0]];
        [self.workAuthorizationsHeaderLabel addConstraint:[NSLayoutConstraint constraintWithItem:self.workAuthorizationsHeaderLabel attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:0]];
    }
    
    if(self.companyData.company_address != nil && ![self.companyData.company_address isEqualToString:@""]){
        self.addressBodyLabel.text = self.companyData.company_address;
    }
    else{
        [self.addressHeaderLabel addConstraint:[NSLayoutConstraint constraintWithItem:self.addressHeaderLabel attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:0]];
        [self.addressBodyLabel addConstraint:[NSLayoutConstraint constraintWithItem:self.addressBodyLabel attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:0]];
    }
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    
    FAKIonIcons *back = [FAKIonIcons ios7ArrowBackIconWithSize:17];
    self.backBtn.title = @"";
    self.backBtn.image = [back imageWithSize:CGSizeMake(30, 30)];
    [self.backBtn setTarget:self];
    [self.backBtn setAction:@selector(exitDetail)];
    
    
    FAKFontAwesome *viewOnMapIcon = [FAKFontAwesome mapMarkerIconWithSize:17];
    self.viewOnMapBtn.title = @"";
    self.viewOnMapBtn.image = [viewOnMapIcon imageWithSize:CGSizeMake(30, 30)];
    [self.viewOnMapBtn setTarget:self];
    [self.viewOnMapBtn setAction:@selector(viewOnMap)];
}

- (void) viewWillAppear:(BOOL)animated{
    id<GAITracker> tracker = [[GAI sharedInstance] defaultTracker];
    [tracker set:kGAIScreenName value:@"Company Detail"];
    [tracker send:[[GAIDictionaryBuilder createScreenView] build]];
}

- (void) exitDetail{
    [self dismissViewControllerAnimated:true completion:nil];
}

- (void) viewOnMap{
    UIViewController* presentingController = ((UINavigationController*)((UITabBarController*)self.presentingViewController).selectedViewController).topViewController;
    if([presentingController isKindOfClass:[CompaniesViewController class]]){
        CompaniesViewController* companiesViewController = (CompaniesViewController*) presentingController;
        companiesViewController.displayOnMap = true;
    }
    
    ((AppDelegate*)[[UIApplication sharedApplication] delegate]).hightlightTableId = @(self.companyData.company_table);
    
    [self dismissViewControllerAnimated:true completion:nil];
}
@end
