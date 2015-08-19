//
//  CFTableDataMap.m
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/10/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import "CFTableDataMap.h"
#import "CFTableData.h"
#import "CFTableMapping.h"
#import "RHCareerFairLayout-Swift.h"

@implementation CFTableDataMap

- (id) initWithTableData: (FMResultSet*) data{
    self = [super init];
    
    if(self){
        self.tableDict = [[NSMutableDictionary alloc] init];
        while([data next]){
            CFTableMapping* tableMapping = [[CFTableMapping alloc] initWithId:[data intForColumn:DBStatements.KEY_ID]
                                                                withCompanyId:[[NSNumber alloc] initWithInt:[data intForColumn:DBStatements.KEY_COMPANY_ID]]
                                                                     withSize:[data intForColumn:DBStatements.KEY_SIZE]];
            CFTableData* tableData = [[CFTableData alloc] initWithTableMapping:tableMapping
                                                                 withRectangle:nil
                                                                      selected:[data intForColumn:DBStatements.KEY_SELECTED]];
            [self.tableDict setObject:tableData forKeyedSubscript:@(tableMapping.id)];
        }
        return self;
    }
    return nil;
}

- (void)removeObjectForKey:(id)aKey{
    return [self.tableDict removeObjectForKey: aKey];
}
- (void)setObject:(id)anObject forKey:(id <NSCopying>)aKey{
    return [self.tableDict setObject:anObject forKey:aKey];
}
- (id)objectForKey:(id)aKey{
    return [self.tableDict objectForKey:aKey];
}
- (NSEnumerator *)keyEnumerator{
    return [self.tableDict keyEnumerator];
}

@end
