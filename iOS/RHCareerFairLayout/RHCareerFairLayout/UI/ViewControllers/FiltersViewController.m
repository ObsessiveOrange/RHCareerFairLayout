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
#import <FontAwesomeKit/FontAwesomeKit.h>

@interface FiltersViewController ()

@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *moreMenuIcon;
@property (nonatomic, strong) NSMutableDictionary* filters;
@property (nonatomic, strong) NSMutableArray* filterTypes;

@end

@implementation FiltersViewController

static NSString* filtersCellReuseIdentifier = @"FilterCell";

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    //     self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    [self.navigationController.navigationBar setBarStyle:UIBarStyleBlack];
    self.navigationController.navigationBar.barTintColor = RHCareerFairLayout.color_primary;
    self.navigationController.navigationBar.tintColor = [UIColor whiteColor];
    self.navigationItem.title = @"Filters";
    
    FAKIonIcons *checkbox = [FAKIonIcons androidMoreIconWithSize:17];
    self.moreMenuIcon.title = @"";
    self.moreMenuIcon.image = [checkbox imageWithSize:CGSizeMake(30, 30)];
    
    [self getData];
}

- (void) getData{
    
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

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    
    return [self.filterTypes count];
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return self.filterTypes[section];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    return [((NSArray*)[self.filters objectForKey:self.filterTypes[section]]) count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    FiltersCell *cell = [tableView dequeueReusableCellWithIdentifier:filtersCellReuseIdentifier forIndexPath:indexPath];
    
    // Configure the cell...
    //    CFCategoryData* data = (CFCategoryData*) self.filtersList[indexPath.row];
    CFCategoryData* data = (CFCategoryData*) ((NSArray*)[self.filters objectForKey:self.filterTypes[indexPath.section]])[indexPath.row];
    
    
    FAKFontAwesome *checkbox;
    if(data.category_selected){
        checkbox = [FAKFontAwesome checkSquareOIconWithSize:15];
    }
    else{
        checkbox = [FAKFontAwesome squareOIconWithSize:15];
    }
    
    [checkbox addAttribute:NSForegroundColorAttributeName value:[UIColor
                                                                 grayColor]];
    
    [cell.filterSelected setAttributedTitle:[checkbox attributedString] forState:UIControlStateNormal];
    cell.filterName.text= data.category_name;
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    //toggle company selected
    CFCategoryData* filter = ((NSArray*)[self.filters objectForKey:self.filterTypes[indexPath.section]])[indexPath.row];
    
    [DBManager setCategory:filter.id selected:!filter.category_selected];
    [self getData];
    [self.tableView reloadData];
}

- (IBAction)showMenuActionSheet:(id)sender {
    
    UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:@"Filters Options Menu"
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                               destructiveButtonTitle:nil
                                                    otherButtonTitles:@"Clear Filters", @"Refresh Data", @"About", nil];
    
    [actionSheet showInView:self.view];
    
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    UIViewController* parent;
    
    switch(buttonIndex){
        case 0: //clear filters
            [DBManager clearSelectedCategories];
            [self getData];
            [self.tableView reloadData];
            break;
        case 1: //refresh data
            
            parent = [self parentViewController];
            
            while(![parent isKindOfClass:[TabBarController class]]){
                parent = [parent parentViewController];
            }
            
            [parent performSegueWithIdentifier:@"ReloadSegue" sender:self];
            
            break;
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

/*
 // Override to support conditional editing of the table view.
 - (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
 // Return NO if you do not want the specified item to be editable.
 return YES;
 }
 */

/*
 // Override to support editing the table view.
 - (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
 if (editingStyle == UITableViewCellEditingStyleDelete) {
 // Delete the row from the data source
 [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
 } else if (editingStyle == UITableViewCellEditingStyleInsert) {
 // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
 }
 }
 */

/*
 // Override to support rearranging the table view.
 - (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
 }
 */

/*
 // Override to support conditional rearranging of the table view.
 - (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
 // Return NO if you do not want the item to be re-orderable.
 return YES;
 }
 */

/*
 #pragma mark - Navigation
 
 // In a storyboard-based application, you will often want to do a little preparation before navigation
 - (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
 // Get the new view controller using [segue destinationViewController].
 // Pass the selected object to the new view controller.
 }
 */

@end
