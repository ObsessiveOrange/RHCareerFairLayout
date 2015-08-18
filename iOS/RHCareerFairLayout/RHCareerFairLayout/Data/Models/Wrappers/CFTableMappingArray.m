//
//  TableMappingArray.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFTableMappingArray.h"

@implementation CFTableMappingArray

- (id) init{
    
    
    self = [super init];
    
    if(self){
        
        self.tableMappingArray = [[NSMutableArray alloc] init];
        
        return self;
    }
    return nil;
}

- (id) initWithArray:(NSArray *) data{
    
    self = [super init];
    
    if(self){
        
        self.tableMappingArray = [[NSMutableArray alloc] init];
        
        for(id obj in data){
            
            NSDictionary* tableMappingData = (NSDictionary*) obj;
            
            NSInteger id = [((NSString*)[tableMappingData objectForKey:@"id"]) integerValue];
            
            NSString* companyIdString = (NSString*)[tableMappingData objectForKey:@"companyId"];
            NSNumber* companyId = [companyIdString isEqual:[NSNull null]] ? nil : [[NSNumber alloc] initWithInteger:[companyIdString integerValue]];
            NSInteger size = [((NSString*)[tableMappingData objectForKey:@"size"]) integerValue];
            
            
            
            CFTableMapping* tableMapping = [[CFTableMapping alloc] initWithId: id
                                                                withCompanyId: companyId
                                                                     withSize: size];
            
            [self addObject:tableMapping];
        }
        
        
        return self;
    }
    return nil;
}


- (id)objectAtIndex:(NSUInteger)index{
    return [self.tableMappingArray objectAtIndex:index];
}
- (void)addObject:(id)anObject{
    return [self.tableMappingArray addObject:anObject];
    
}
- (void)insertObject:(id)anObject atIndex:(NSUInteger)index{
    return [self.tableMappingArray insertObject:anObject atIndex:index];
    
}
- (void)removeLastObject{
    return [self.tableMappingArray removeLastObject];
    
}
- (void)removeObjectAtIndex:(NSUInteger)index{
    return [self.tableMappingArray removeObjectAtIndex:index];
    
}
- (void)replaceObjectAtIndex:(NSUInteger)index withObject:(id)anObject{
    return [self.tableMappingArray replaceObjectAtIndex:index withObject:anObject];
    
}

@end
