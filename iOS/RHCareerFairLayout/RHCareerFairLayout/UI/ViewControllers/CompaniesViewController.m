//
//  CompaniesViewController.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/4/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "RHCareerFairLayout-Swift.h"
#import "CompaniesViewController.h"
#import "CompaniesCell.h"
#import "CFCompanyData.h"
#import "DBManager.h"
#import "TabBarController.h"
#import "AppDelegate.h"
#import <FontAwesomeKit/FontAwesomeKit.h>

@interface CompaniesViewController ()

@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (strong, nonatomic) IBOutlet UIBarButtonItem *moreMenuBtn;
@property (strong, nonatomic) IBOutlet UIBarButtonItem *exitSearchBtn;
@property (nonatomic, strong) NSArray* companyList;
@property (nonatomic, strong) UIView* pageTitleView;
@property (nonatomic, strong) UISearchBar* pageSearchView;
@property (nonatomic) bool searchActive;

@end

@implementation CompaniesViewController

static NSString* companiesCellReuseIdentifier = @"CompanyCell";

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    [self.navigationController.navigationBar setBarStyle:UIBarStyleBlack];
    self.navigationController.navigationBar.barTintColor = RHCareerFairLayout.color_primary;
    self.navigationController.navigationBar.tintColor = [UIColor whiteColor];
    self.navigationItem.title = @"Companies";
    
    [self hideSearch];
    
    self.pageSearchView = [[UISearchBar alloc] initWithFrame:CGRectMake(0, 0, 320, 64)];
    self.pageSearchView.delegate = self;
    self.pageSearchView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    
    self.pageTitleView = self.navigationItem.titleView;
    
    FAKIonIcons *checkbox = [FAKIonIcons androidMoreIconWithSize:17];
    self.moreMenuBtn.title = @"";
    self.moreMenuBtn.image = [checkbox imageWithSize:CGSizeMake(30, 30)];
    
    FAKIonIcons *back = [FAKIonIcons ios7ArrowBackIconWithSize:17];
    self.exitSearchBtn.title = @"";
    self.exitSearchBtn.image = [back imageWithSize:CGSizeMake(30, 30)];
    self.exitSearchBtn.target = self;
    self.exitSearchBtn.action = @selector(hideSearch);
}

- (void) hideSearch{
    self.searchActive = false;
    self.navigationItem.leftBarButtonItem = nil;
    self.navigationItem.rightBarButtonItem = self.moreMenuBtn;
    self.navigationItem.titleView = self.pageTitleView;
}

- (void) showSearch{
    self.searchActive = true;
    self.navigationItem.leftBarButtonItem = self.exitSearchBtn;
    self.navigationItem.rightBarButtonItem = nil;
    self.navigationItem.titleView = self.pageSearchView;
}

-(void)viewDidAppear:(BOOL)animated{
    [super viewDidAppear:animated];
    
    self.companyList = [DBManager getFilteredCompanies];
    [self.tableView reloadData];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    return [self.companyList count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    CompaniesCell *cell = [tableView dequeueReusableCellWithIdentifier:companiesCellReuseIdentifier forIndexPath:indexPath];
    
    // Configure the cell...
    CFCompanyData* data = (CFCompanyData*) self.companyList[indexPath.row];
    
    FAKFontAwesome *checkbox;
    if(data.company_selected){
        checkbox = [FAKFontAwesome checkSquareOIconWithSize:15];
    }
    else{
        checkbox = [FAKFontAwesome squareOIconWithSize:15];
    }
    
    [checkbox addAttribute:NSForegroundColorAttributeName value:[UIColor
                                                                 grayColor]];
    
    [cell.showOnMap setAttributedTitle:[checkbox attributedString] forState:UIControlStateNormal];
    cell.showOnMap.tag = indexPath.row;
    [cell.showOnMap addTarget:self action:@selector(checkBoxSelected:) forControlEvents:UIControlEventTouchUpInside];
    
    [cell.companyName setTitle:data.company_name
                      forState:UIControlStateNormal];
    [cell.companyName.titleLabel setTextAlignment: NSTextAlignmentCenter];
    cell.companyName.titleLabel.numberOfLines = 2;
    cell.companyName.tag = indexPath.row;
    [cell.companyName addTarget:self action:@selector(companyNameSelected:) forControlEvents:UIControlEventTouchUpInside];
    
    [cell.tableNumber setTitle:[[NSString alloc] initWithFormat:@"%@", data.company_table]
                      forState:UIControlStateNormal];
    cell.tableNumber.tag = indexPath.row;
    [cell.tableNumber addTarget:self action:@selector(companyTableSelected:) forControlEvents:UIControlEventTouchUpInside];
    
    return cell;
}

- (void)checkBoxSelected:(UIButton*)sender
{
    CFCompanyData* company = (CFCompanyData*)self.companyList[sender.tag];
    [DBManager setCompany: company.id selected:!company.company_selected];
    self.companyList = [DBManager getFilteredCompanies];
    [self.tableView reloadData];
    
    [self.view endEditing:YES];
}

- (void)companyNameSelected:(UILabel*)sender
{
    
    CFCompanyData* company = (CFCompanyData*)self.companyList[sender.tag];
    NSLog(@"Show detail for companyId: %@", company.id);
    
    [self.view endEditing:YES];
    
}

- (void)companyTableSelected:(UILabel*)sender
{
    
    CFCompanyData* company = (CFCompanyData*)self.companyList[sender.tag];
    NSLog(@"Show map for companyId: %@", company.id);
    
    [self.view endEditing:YES];
    
}

- (void) tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    
    [self.view endEditing:YES];
    
}

- (IBAction)showMenuActionSheet:(id)sender {
    
    UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:@"Companies Options Menu"
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                               destructiveButtonTitle:nil
                                                    otherButtonTitles:@"Search", @"Select All", @"Deselect All", @"Refresh Data", @"About", nil];
    
    [self.view endEditing:YES];
    
    [actionSheet showInView:self.view];
    
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    UIViewController* parent;
    
    switch(buttonIndex){
        case 0: //search
            [self showSearch];
            break;
        case 1: //select all
            [DBManager updateAllSelectedCompaniesWithSelected:true];
            self.companyList = [DBManager getFilteredCompanies];
            [self.tableView reloadData];
            break;
        case 2: //deselect all
            [DBManager updateAllSelectedCompaniesWithSelected:false];
            self.companyList = [DBManager getFilteredCompanies];
            [self.tableView reloadData];
            break;
        case 3: //refresh data
            
            parent = [self parentViewController];
            
            while(![parent isKindOfClass:[TabBarController class]]){
                parent = [parent parentViewController];
            }
            
            [parent performSegueWithIdentifier:@"ReloadSegue" sender:self];
            
            break;
        case 4: //about
            [[[UIAlertView alloc] initWithTitle:@"About"
                                        message:RHCareerFairLayout.aboutString
                                       delegate:self
                              cancelButtonTitle:@"OK"
                              otherButtonTitles:nil] show];
            break;
    }
    
}

- (void) searchBar:(UISearchBar *)searchBar textDidChange:(NSString *)searchText{
    
    AppDelegate* delegate = ((AppDelegate*)[[UIApplication sharedApplication] delegate]);
    delegate.searchText = searchText;
    
    self.companyList = [DBManager getFilteredCompanies];
    [self.tableView reloadData];
    
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
