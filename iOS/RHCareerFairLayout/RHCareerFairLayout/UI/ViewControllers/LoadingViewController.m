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
#import "AppDelegate.h"
#import "DBManager.h"

@interface LoadingViewController ()
@property (weak, nonatomic) IBOutlet UILabel *statusLabel;
@property (weak, nonatomic) IBOutlet UIButton *button;

@end

@implementation LoadingViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)viewDidAppear:(BOOL)animated{
    
    [self retreiveData];
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}
- (IBAction)retreiveData {
    
    [self.statusLabel setText:@"Downloading new data..."];
    self.button.hidden = true;
    [self.view setNeedsLayout];
    
    //    NSLog(@"%ld", (long)[@(floor([[NSDate date] timeIntervalSince1970] * 1000)) integerValue]);
    //    NSLog(@"%f", [DBManager getTerm].term_lastUpdateTime);
    
    if(!((AppDelegate*)[[UIApplication sharedApplication] delegate]).forceReload &&
       [[NSDate date] compare:[[DBManager getTerm].term_lastUpdateTime dateByAddingTimeInterval:60*60*24]] == NSOrderedAscending){
        
        NSLog(@"Data up-to-date.");
        [self performSegueWithIdentifier:@"finishedLoadingSegue" sender:nil];
    }
    else{
        
        NSLog(@"Data outdated or not found. Updating.");
        ((AppDelegate*)[[UIApplication sharedApplication] delegate]).forceReload = false;
        
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            
            NSURL *requestURL = [NSURL URLWithString:@"http://rhcareerfair.cf/api/data/all/latest"];
            //    NSURL *requestURL = [NSURL URLWithString:[NSString stringWithFormat:@"http://rhcareerfair.cf/api/data/all?year=%@&quarter=%@", year, quarter]];
            
            
            NSMutableURLRequest *request = [[NSMutableURLRequest alloc] init];
            [request setURL: requestURL];
            [request setHTTPMethod: @"GET"];
            [request setValue: @"application/json;charset=UTF-8" forHTTPHeaderField:@"Content-Type"];
            
            
            NSURLResponse *requestResponse;
            NSError *requestError;
            NSData *responseData = [NSURLConnection sendSynchronousRequest:request returningResponse:&requestResponse error:&requestError];
            
            
            if(requestError != nil){
                dispatch_async(dispatch_get_main_queue(), ^{
                    NSLog(@"Error while retreiving data: %@", requestError);
                    
                    self.statusLabel.text = @"Failed to download data. Do you have internet access?";
                    self.button.hidden = false;
                });
                return;
            }
            
            self.statusLabel.text = @"Parsing new data...";
            [self.view setNeedsLayout];
            
            // Get JSON data into a Foundation object
            NSError *error = nil;
            NSDictionary* responseDictionary = [NSJSONSerialization JSONObjectWithData:responseData options:NSJSONReadingAllowFragments error:&error];
            
            if(error != nil){
                dispatch_async(dispatch_get_main_queue(), ^{
                    NSLog(@"Error while retreiving data: %@", requestError);
                    
                    self.statusLabel.text = @"Failed to download data. Do you have internet access?";
                    self.button.hidden = false;
                });
                return;
            }
            
            CFDataWrapper* careerFairData = [[CFDataWrapper alloc] initWithDictionary:responseDictionary];
            
            NSLog(@"Data parsed");
            
            [DBManager loadNewData:careerFairData];
            
            dispatch_async(dispatch_get_main_queue(), ^{
                NSLog(@"%@", @"Done loading data.");
                
                [self performSegueWithIdentifier:@"finishedLoadingSegue" sender:nil];
            });
            return;
        });
    }
}

@end
