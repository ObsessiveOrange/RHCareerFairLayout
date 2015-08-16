//
//  TableMappingArray.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFTableMappingArray.h"

@implementation CFTableMappingArray

- (id) initWithArray:(NSArray *) data{
    
    CFTableMappingArray* array = [super init];
    
    if(array){
        
        self.tableMappingArray = [[NSMutableArray alloc] init];
        
        for(id obj in data){
            
            NSDictionary* tableMappingData = (NSDictionary*) obj;
            
            NSNumber* id = [[NSNumber alloc] initWithInteger:[((NSString*)[tableMappingData objectForKey:@"id"]) integerValue]];
            
            NSString* companyIdString = (NSString*)[tableMappingData objectForKey:@"companyId"];
            NSNumber* companyId = [companyIdString isEqual:[NSNull null]] ? nil : [[NSNumber alloc] initWithInteger:[companyIdString integerValue]];
            NSNumber* size = [[NSNumber alloc] initWithInteger:[((NSString*)[tableMappingData objectForKey:@"size"]) integerValue]];
            
            
            
            CFTableMapping* tableMapping = [[CFTableMapping alloc] initWithId: id
                                                                withCompanyId: companyId
                                                                     withSize: size];
            
            [self addObject:tableMapping];
        }
        
        
        return array;
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
