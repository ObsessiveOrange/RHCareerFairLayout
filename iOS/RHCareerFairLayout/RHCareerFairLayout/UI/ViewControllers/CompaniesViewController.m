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
    
    self.displayOnMap = false;
    
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
    
    FAKIonIcons *more = [FAKIonIcons androidMoreIconWithSize:17];
    self.moreMenuBtn.title = @"";
    self.moreMenuBtn.image = [more imageWithSize:CGSizeMake(30, 30)];
    [self.navigationItem setRightBarButtonItem:self.moreMenuBtn];
    
    
    FAKIonIcons *back = [FAKIonIcons ios7ArrowBackIconWithSize:17];
    self.exitSearchBtn.title = @"";
    self.exitSearchBtn.image = [back imageWithSize:CGSizeMake(30, 30)];
    [self.navigationItem setLeftBarButtonItem:self.exitSearchBtn];
    
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
    
    if(self.displayOnMap){
        self.displayOnMap = false;
        
        [self gotoMapView];
    }
    
    self.companyList = [DBManager getFilteredCompanies];
    [self.tableView reloadData];
}

-(void)viewDidAppear:(BOOL)animated{
    [super viewDidAppear:animated];
    
    if(self.displayOnMap){
        self.displayOnMap = false;
        
        [self.tabBarController setSelectedIndex:0];
    }
}

-(void) gotoMapView{
    
    [self.tabBarController setSelectedIndex:0];
    [[self.tabBarController selectedViewController] viewDidAppear:true];
}

- (IBAction)hideSearch:(UIBarButtonItem *)sender {
    [self hideSearch];
}

- (void) hideSearch{
    
    [self.pageSearchView endEditing:YES];
    
    self.searchActive = false;
    [self.navigationItem.leftBarButtonItem setEnabled:false];
        [self.navigationItem.leftBarButtonItem setTintColor: [UIColor clearColor]];
//    self.navigationItem.leftBarButtonItem = nil;
//    self.navigationItem.rightBarButtonItem = self.moreMenuBtn;
    self.navigationItem.titleView = self.pageTitleView;
}

- (void) showSearch{
    self.searchActive = true;
    [self.navigationItem.leftBarButtonItem setEnabled:true];
        [self.navigationItem.leftBarButtonItem setTintColor: nil];
//    self.navigationItem.leftBarButtonItem = self.exitSearchBtn;
//    self.navigationItem.rightBarButtonItem = nil;
    self.navigationItem.titleView = self.pageSearchView;
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    return 1;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    
    NSString* searchText = ((AppDelegate*)[[UIApplication sharedApplication] delegate]).searchText;
    
    return [searchText length] == 0 ? nil : [[NSString alloc] initWithFormat:@"Searched: %@", searchText];
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
        checkbox = checkedCheckbox;
    }
    else{
        checkbox = uncheckedCheckbox;
    }

    [cell.showOnMap setAttributedTitle:[checkbox attributedString] forState:UIControlStateNormal];
    cell.showOnMap.tag = indexPath.row;

    [cell.companyName setTitle:data.company_name
                      forState:UIControlStateNormal];
    [cell.companyName.titleLabel setTextAlignment: NSTextAlignmentCenter];
    [cell.companyName.titleLabel setNumberOfLines:2];
    cell.companyName.tag = indexPath.row;
    
    [cell.tableNumber setTitle:[[NSString alloc] initWithFormat:@"%ld", (long)data.company_table]
                      forState:UIControlStateNormal];
    cell.tableNumber.tag = indexPath.row;
    
    return cell;
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView{
    
    [self.pageSearchView endEditing:YES];
}

- (IBAction)checkBoxSelected:(UIButton *)sender
{
    
    [self.pageSearchView endEditing:YES];
    
    CFCompanyData* company = (CFCompanyData*)self.companyList[sender.tag];
    [DBManager setCompany: company.id selected:!company.company_selected];
    self.companyList = [DBManager getFilteredCompanies];
    [self.tableView reloadData];
}
- (IBAction)companyNameSelected:(UIButton *)sender
{
    [self.pageSearchView endEditing:YES];
    
    CFCompanyData* company = (CFCompanyData*)self.companyList[sender.tag];
    self.selectedCompany = company;
    [self performSegueWithIdentifier:RHCareerFairLayout.companyDetailSegueIdentifier sender:self];
    
}
- (IBAction)companyTableSelected:(UIButton*)sender
{
    [self.pageSearchView endEditing:YES];
    
    CFCompanyData* company = (CFCompanyData*)self.companyList[sender.tag];
    NSLog(@"Show map for companyId: %ld", (long)company.id);
    
    ((AppDelegate*)[[UIApplication sharedApplication] delegate]).hightlightTableId = @(company.company_table);
    [self gotoMapView];
    
}

- (void) tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    
    [self.pageSearchView endEditing:YES];
    
}
- (IBAction)showMenuActionSheet:(UIBarButtonItem *)sender {
    
    UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:@"Companies Options Menu"
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                               destructiveButtonTitle:nil
                                                    otherButtonTitles:@"Toggle Search", @"Select All", @"Deselect All", @"Refresh Data", @"About", nil];
    
    [actionSheet showInView:self.view];
    
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    UIViewController* parent;
    
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
