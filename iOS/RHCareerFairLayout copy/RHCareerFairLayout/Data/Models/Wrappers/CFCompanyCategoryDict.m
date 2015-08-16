//
//  CompanyCategoryDict.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFCompanyCategoryDict.h"

@implementation CFCompanyCategoryDict

- (id) initWithDictionary:(NSDictionary *) data{
    
    self = [super init];
    
    if(self){
        
        self.companyCategoryDict = [[NSMutableDictionary alloc] init];
        
        for(id key in data){
            
            NSDictionary* companyCategoryData = (NSDictionary*) [data objectForKey:key];
            
            NSNumber* companyId = [[NSNumber alloc] initWithInt:(int)[companyCategoryData objectForKey:@"companyId"]];
            NSArray* categories = (NSArray*)[companyCategoryData objectForKey:@"categories"];
            
            
            
            CFCompanyCategory* companyCategory = [[CFCompanyCategory alloc] initWithCompanyId: companyId
                                                         withCategories: categories];
            
            [self setObject:companyCategory forKey:companyId];
        }
        
        return self;
    }
    
    return nil;
}

- (void)removeObjectForKey:(id)aKey{
    return [self.companyCategoryDict removeObjectForKey: aKey];
}
- (void)setObject:(id)anObject forKey:(id <NSCopying>)aKey{
    return [self.companyCategoryDict setObject:anObject forKey:aKey];
}
- (id)objectForKey:(id)aKey{
    return [self.companyCategoryDict objectForKey:aKey];
}
- (NSEnumerator *)keyEnumerator{
    return [self.companyCategoryDict keyEnumerator];
}

@end
