//
//  LoadingViewController.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "LoadingViewController.h"
#import "RHCareerFairLayout-Swift.h"
#import "CFDataWrapper.h"
#import "DBManager.h"

@interface LoadingViewController ()

@end

@implementation LoadingViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Do any additional setup after loading the view.
    
    self.navigationController.navigationBar.barTintColor = RHCareerFairLayout.color_primary;
}

- (void)viewDidAppear:(BOOL)animated{
    
    [self retreiveDataForYear:@"2015" forQuarter:@"Fall"];
    
    NSLog(@"%@", @"Done loading data.");
    [self performSegueWithIdentifier:@"finishedLoadingSegue" sender:nil];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)retreiveDataForYear: (NSString*) year forQuarter: (NSString*) quarter{
    
    
    NSURL *requestURL = [NSURL URLWithString:@"http://rhcareerfair.cf/api/data/all/latest"];
//    NSURL *requestURL = [NSURL URLWithString:[NSString stringWithFormat:@"http://rhcareerfair.cf/api/data/all?year=%@&quarter=%@", year, quarter]];
    
    
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] init];
    [request setURL: requestURL];
    [request setHTTPMethod: @"GET"];
    [request setValue: @"application/json;charset=UTF-8" forHTTPHeaderField:@"Content-Type"];
    
    
    NSURLResponse *requestResponse;
    NSError *requestError;
    NSData *responseData = [NSURLConnection sendSynchronousRequest:request returningResponse:&requestResponse error:&requestError];
    
    // Get JSON data into a Foundation object
    NSError *error = nil;
    NSDictionary* responseDictionary = [NSJSONSerialization JSONObjectWithData:responseData options:NSJSONReadingAllowFragments error:&error];
    
    CFDataWrapper* careerFairData = [[CFDataWrapper alloc] initWithDictionary:responseDictionary];
    
    NSLog(@"Data parsed");
    
    [DBManager loadNewData:careerFairData];
}

@end
