//
//  CategoryDict.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFCategoryDict.h"

@implementation CFCategoryDict

- (id) initWithDictionary:(NSDictionary *) data{
    
    self = [super init];
    
    if(self){
        
        self.categoryDict = [[NSMutableDictionary alloc] init];
        
        for(id key in data){
            
            NSDictionary* categoryData = (NSDictionary*) [data objectForKey:key];
            
            NSNumber* id = [[NSNumber alloc] initWithInt:(int)[categoryData objectForKey:@"id"]];
            NSString* name = (NSString*)[categoryData objectForKey:@"name"];
            NSString* type = (NSString*)[categoryData objectForKey:@"type"];
            
            
            
            CFCategory* category = [[CFCategory alloc] initWithId: id
                                                         withName: name
                                                         withType: type];
            
            [self setObject:category forKey:id];
        }
        
        return self;
    }
    
    return nil;
}

- (void)removeObjectForKey:(id)aKey{
    return [self.categoryDict removeObjectForKey: aKey];
}
- (void)setObject:(id)anObject forKey:(id <NSCopying>)aKey{
    return [self.categoryDict setObject:anObject forKey:aKey];
}
- (id)objectForKey:(id)aKey{
    return [self.categoryDict objectForKey:aKey];
}
- (NSEnumerator *)keyEnumerator{
    return [self.categoryDict keyEnumerator];
}

@end
