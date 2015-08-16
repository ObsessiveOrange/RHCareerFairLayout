//
//  CompanyDict.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFCompanyDict.h"

@implementation CFCompanyDict

- (id) initWithDictionary:(NSDictionary *) data{
    
    self = [super init];
    
    if(self){
        
        self.companyDict = [[NSMutableDictionary alloc] init];
        
        for(id key in data){
            
            NSDictionary* companyData = (NSDictionary*) [data objectForKey:key];
            
            NSNumber* id = [[NSNumber alloc] initWithInt:(int)[companyData objectForKey:@"id"]];
            NSString* name = (NSString*)[companyData objectForKey:@"name"];
            NSString* description = (NSString*)[companyData objectForKey:@"description"];
            NSString* websiteLink = (NSString*)[companyData objectForKey:@"websiteLink"];
            NSString* address = (NSString*)[companyData objectForKey:@"address"];
            
            
            
            CFCompany* company = [[CFCompany alloc] initWithId: id
                                                      withName: name
                                               withDescription: description
                                               withWebsiteLink: websiteLink
                                               withAddress: address];
            
            [self setObject:company forKey:id];
        }
        
        return self;
    }
    
    return nil;
}

- (void)removeObjectForKey:(id)aKey{
    return [self.companyDict removeObjectForKey: aKey];
}
- (void)setObject:(id)anObject forKey:(id <NSCopying>)aKey{
    return [self.companyDict setObject:anObject forKey:aKey];
}
- (id)objectForKey:(id)aKey{
    return [self.companyDict objectForKey:aKey];
}
- (NSEnumerator *)keyEnumerator{
    return [self.companyDict keyEnumerator];
}

@end
