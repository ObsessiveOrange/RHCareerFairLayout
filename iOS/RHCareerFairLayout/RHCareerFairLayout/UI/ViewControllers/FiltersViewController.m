//
//  FiltersViewController.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/4/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "RHCareerFairLayout-Swift.h"
#import "FiltersViewController.h"
#import "FiltersCell.h"
#import "CFCategoryData.h"
#import "DBManager.h"
#import "TabBarController.h"
#import "AppDelegate.h"
#import <FontAwesomeKit/FontAwesomeKit.h>
#import <Google/Analytics.h>

@interface FiltersViewController ()

@property (weak, nonatomic) IBOutlet UIBarButtonItem *moreMenuIcon;
@property (nonatomic, strong) NSMutableDictionary* filters;
@property (nonatomic, strong) NSMutableArray* filterTypes;

@end

@implementation FiltersViewController

static NSString* filtersCellReuseIdentifier = @"FilterCell";

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Setup navigationBar theme
    [self.navigationController.navigationBar setBarStyle:UIBarStyleBlack];
    self.navigationController.navigationBar.barTintColor = RHCareerFairLayout.color_primary;
    self.navigationController.navigationBar.tintColor = [UIColor whiteColor];
    self.navigationItem.title = @"Filters";
    
    // Setup icon for actionSheet menu
    FAKIonIcons *checkbox = [FAKIonIcons androidMoreIconWithSize:20];
    self.moreMenuIcon.title = @"";
    self.moreMenuIcon.image = [checkbox imageWithSize:CGSizeMake(30, 30)];
    
    [self getData];
}

- (void) viewWillAppear:(BOOL)animated{
    id<GAITracker> tracker = [[GAI sharedInstance] defaultTracker];
    [tracker set:kGAIScreenName value:@"Filters"];
    [tracker send:[[GAIDictionaryBuilder createScreenView] build]];
}

- (void) getData{
    
    // Get, split up categories based on type.
    NSArray* filtersList = [DBManager getCategories];
    self.filters = [[NSMutableDictionary alloc] init];
    self.filterTypes = [[NSMutableArray alloc] init];
    
    for(int i = 0; i < [filtersList count]; i++){
        
        CFCategoryData* filter = filtersList[i];
        
        if(![self.filters objectForKey:filter.category_type]){
            [self.filters setObject:[[NSMutableArray alloc] init]
                             forKey:filter.category_type];
            [self.filterTypes addObject:filter.category_type];
        }
        
        [((NSMutableArray*)[self.filters objectForKey:filter.category_type]) addObject:filter];
    }
    
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    return [self.filterTypes count];
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    // Display header for each type
    return self.filterTypes[section];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    return [((NSArray*)[self.filters objectForKey:self.filterTypes[section]]) count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    FiltersCell *cell = [tableView dequeueReusableCellWithIdentifier:filtersCellReuseIdentifier forIndexPath:indexPath];
    
    // Get category data,
    CFCategoryData* data = (CFCategoryData*) ((NSArray*)[self.filters objectForKey:self.filterTypes[indexPath.section]])[indexPath.row];
    
    // Select appropriate checkbox, add to cell
    FAKFontAwesome *checkbox;
    if(data.category_selected){
        checkbox = [FAKFontAwesome checkSquareOIconWithSize:15];
    }
    else{
        checkbox = [FAKFontAwesome squareOIconWithSize:15];
    }
    [checkbox addAttribute:NSForegroundColorAttributeName value:[UIColor
                                                                 grayColor]];
    
    // Set title
    [cell.filterSelected setAttributedTitle:[checkbox attributedString] forState:UIControlStateNormal];
    cell.filterName.text= data.category_name;
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Toggle company selected
    CFCategoryData* filter = ((NSArray*)[self.filters objectForKey:self.filterTypes[indexPath.section]])[indexPath.row];
    [DBManager setCategory:filter.id selected:!filter.category_selected];
    
    // Get new cursor, parse and reload.
    [self getData];
    [self.tableView reloadData];
}

- (IBAction)showMenuActionSheet:(id)sender {
    
    UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:nil
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                               destructiveButtonTitle:nil
                                                    otherButtonTitles:@"Clear Filters", @"Refresh Data", @"About", nil];
    
    [actionSheet showInView:self.view];
    
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    switch(buttonIndex){
        case 0: //clear filters
            [DBManager clearSelectedCategories];
            [self getData];
            [self.tableView reloadData];
            break;
        case 1: //refresh data
            
            // Set force reload flag - no cache.
            ((AppDelegate*)[[UIApplication sharedApplication] delegate]).forceReload = true;
            
            // Send reload request
            [[TabBarController instance] performSegueWithIdentifier:@"ReloadSegue" sender:self];
            
            break;
        case 2: //about
            [[[UIAlertView alloc] initWithTitle:@"About"
                                        message:RHCareerFairLayout.aboutString
                                       delegate:self
                              cancelButtonTitle:@"OK"
                              otherButtonTitles:nil] show];
            break;
    }
    
}

@end
