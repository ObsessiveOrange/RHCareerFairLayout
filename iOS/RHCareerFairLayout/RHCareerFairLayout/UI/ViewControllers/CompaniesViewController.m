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
#import "DetailViewController.h"
#import "TabBarController.h"
#import "AppDelegate.h"
#import <FontAwesomeKit/FontAwesomeKit.h>
#import <Google/Analytics.h>

@interface CompaniesViewController ()

@property (strong, nonatomic) IBOutlet UIBarButtonItem *moreMenuBtn;
@property (strong, nonatomic) IBOutlet UIBarButtonItem *exitSearchBtn;
@property (nonatomic, strong) NSArray* companyList;
@property (nonatomic, strong) UIView* pageTitleView;
@property (nonatomic, strong) UISearchBar* pageSearchView;
@property (nonatomic) bool searchActive;

@property (strong, nonatomic) CFCompanyData* selectedCompany;

@end

@implementation CompaniesViewController

static NSString* companiesCellReuseIdentifier = @"CompanyCell";
static FAKFontAwesome* checkedCheckbox;
static FAKFontAwesome* uncheckedCheckbox;

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Setup flag for companyDetail return
    self.displayOnMap = false;
    
    // Setup navigationBar theme
    [self.navigationController.navigationBar setBarStyle:UIBarStyleBlack];
    self.navigationController.navigationBar.barTintColor = RHCareerFairLayout.color_primary;
    self.navigationController.navigationBar.tintColor = [UIColor whiteColor];
    self.navigationItem.title = @"Companies";
    
    self.pageTitleView = self.navigationItem.titleView;
    
    // Search hidden by default
    [self hideSearch];
    self.pageSearchView = [[UISearchBar alloc] initWithFrame:CGRectMake(0, 0, 320, 64)];
    self.pageSearchView.delegate = self;
    self.pageSearchView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    
    // Setup icons
    FAKIonIcons *more = [FAKIonIcons androidMoreIconWithSize:20];
    self.moreMenuBtn.title = @"";
    self.moreMenuBtn.image = [more imageWithSize:CGSizeMake(30, 30)];
    [self.navigationItem setRightBarButtonItem:self.moreMenuBtn];
    
    FAKIonIcons *back = [FAKIonIcons ios7ArrowBackIconWithSize:20];
    self.exitSearchBtn.title = @"";
    self.exitSearchBtn.image = [back imageWithSize:CGSizeMake(30, 30)];
    [self.navigationItem setLeftBarButtonItem:self.exitSearchBtn];
    
    // Generate check/unchecked icons
    checkedCheckbox = [FAKFontAwesome checkSquareOIconWithSize:15];
    [checkedCheckbox addAttribute:NSForegroundColorAttributeName value:[UIColor
                                                                        grayColor]];
    uncheckedCheckbox = [FAKFontAwesome squareOIconWithSize:15];
    [uncheckedCheckbox addAttribute:NSForegroundColorAttributeName value:[UIColor
                                                                          grayColor]];
    
    [self hideSearch];
}

-(void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    
    // If coming back from the detailView with show on map selected, redirect here.
    if(self.displayOnMap){
        self.displayOnMap = false;
        
        [self gotoMapView];
        return;
    }
    
    self.companyList = [DBManager getFilteredCompanies];
    [self.tableView reloadData];
}

-(void)viewDidAppear:(BOOL)animated{
    [super viewDidAppear:animated];
    
    id<GAITracker> tracker = [[GAI sharedInstance] defaultTracker];
    [tracker set:kGAIScreenName value:@"Companies"];
    [tracker send:[[GAIDictionaryBuilder createScreenView] build]];
}

-(void) gotoMapView{
    
    // Redirect, and trigger viewDidAppear.
    
    [self.tabBarController setSelectedIndex:0];
    [[self.tabBarController selectedViewController] viewDidAppear:true];
}

# pragma mark - Search functions

- (IBAction)hideSearch:(UIBarButtonItem *)sender {
    [self hideSearch];
}

- (void) hideSearch{
    
    // Hide keyboard
    [self.pageSearchView endEditing:YES];
    
    // Set flag, hide relevant items.
    self.searchActive = false;
    [self.navigationItem.leftBarButtonItem setEnabled:false];
    [self.navigationItem.leftBarButtonItem setTintColor: [UIColor clearColor]];
    self.navigationItem.titleView = self.pageTitleView;
}

- (void) showSearch{
    
    // Set flag, hide relevant items.
    self.searchActive = true;
    [self.navigationItem.leftBarButtonItem setEnabled:true];
    [self.navigationItem.leftBarButtonItem setTintColor: nil];
    self.navigationItem.titleView = self.pageSearchView;
}

- (void) searchBar:(UISearchBar *)searchBar textDidChange:(NSString *)searchText{
    
    // Get new search string
    [[NSUserDefaults standardUserDefaults] setValue:searchText forKey:@"searchText"];
    
    // Get new cursor, reload
    self.companyList = [DBManager getFilteredCompanies];
    [self.tableView reloadData];
    
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    return 1;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    // Use this header (only ever have one) to show what is currently being searched.
    // A rather hack-ish way of doing it, but it has a good look to it.
    NSString* searchText = [[NSUserDefaults standardUserDefaults] stringForKey:@"searchText"];
    
    // Return nil for no header if no search term.
    return [searchText length] == 0 ? nil : [[NSString alloc] initWithFormat:@"Searched: %@", searchText];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    return [self.companyList count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    CompaniesCell *cell = [tableView dequeueReusableCellWithIdentifier:companiesCellReuseIdentifier forIndexPath:indexPath];
    
    // Configure the cell.
    CFCompanyData* data = (CFCompanyData*) self.companyList[indexPath.row];
    
    // Get appropriate checkbox, set as icon.
    FAKFontAwesome *checkbox;
    if(data.company_selected){
        checkbox = checkedCheckbox;
    }
    else{
        checkbox = uncheckedCheckbox;
    }
    [cell.showOnMap setAttributedTitle:[checkbox attributedString] forState:UIControlStateNormal];
    cell.showOnMap.tag = indexPath.row;
    
    // Set Title
    [cell.companyName setTitle:data.company_name
                      forState:UIControlStateNormal];
    [cell.companyName.titleLabel setNumberOfLines:2];
    [cell.companyName.titleLabel setTextAlignment: NSTextAlignmentCenter];
    cell.companyName.tag = indexPath.row;
    
    // Set Table Number
    [cell.tableNumber setTitle:[[NSString alloc] initWithFormat:@"%ld", (long)data.company_table]
                      forState:UIControlStateNormal];
    
    // Set tag for ease of use.
    cell.tableNumber.tag = indexPath.row;
    
    return cell;
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView{
    
    // Hide keyboard on scroll
    [self.pageSearchView endEditing:YES];
}

#pragma mark - Tap handlers for cells

- (void) tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    
    // Don't do anything except hide keyboard
    [self.pageSearchView endEditing:YES];
    
}

- (IBAction)checkBoxSelected:(UIButton *)sender
{
    // Hide keyboard on tap
    [self.pageSearchView endEditing:YES];
    
    // Update DB, retrieve new cursor, reload.
    CFCompanyData* company = (CFCompanyData*)self.companyList[sender.tag];
    [DBManager setCompany: company.id selected:!company.company_selected];
    self.companyList = [DBManager getFilteredCompanies];
    [self.tableView reloadData];
}

- (IBAction)companyNameSelected:(UIButton *)sender
{
    
    // Hide keyboard on tap
    [self.pageSearchView endEditing:YES];
    
    // Pass data to segue through class-variable.
    CFCompanyData* company = (CFCompanyData*)self.companyList[sender.tag];
    self.selectedCompany = company;
    [self performSegueWithIdentifier:RHCareerFairLayout.companyDetailSegueIdentifier sender:self];
    
}

- (IBAction)companyTableSelected:(UIButton*)sender
{
    
    // Hide keyboard on tap
    [self.pageSearchView endEditing:YES];
    
    // Highlight on map. Pass ID through AppDelegate.
    CFCompanyData* company = (CFCompanyData*)self.companyList[sender.tag];
    ((AppDelegate*)[[UIApplication sharedApplication] delegate]).hightlightTableId = @(company.company_table);
    [self gotoMapView];
    
}

#pragma mark - Menu

- (IBAction)showMenuActionSheet:(UIBarButtonItem *)sender {
    
    UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:@"Companies Options Menu"
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                               destructiveButtonTitle:nil
                                                    otherButtonTitles:@"Toggle Search", @"Select All", @"Deselect All", @"Refresh Data", @"About", nil];
    
    [actionSheet showInView:self.view];
    
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    switch(buttonIndex){
        case 0: //search
            
            if(self.searchActive){
                [self hideSearch];
            }
            else{
                [self showSearch];
            }
            break;
            
        case 1: //select all
            
            [DBManager updateFilteredCompaniesWithSelected:true];
            self.companyList = [DBManager getFilteredCompanies];
            [self.tableView reloadData];
            break;
            
        case 2: //deselect all
            
            [DBManager updateFilteredCompaniesWithSelected:false];
            self.companyList = [DBManager getFilteredCompanies];
            [self.tableView reloadData];
            break;
            
        case 3: //refresh data
            
            // Set force reload flag - no cache.
            ((AppDelegate*)[[UIApplication sharedApplication] delegate]).forceReload = true;
            
            [[TabBarController instance] performSegueWithIdentifier:@"ReloadSegue" sender:self];
            
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
