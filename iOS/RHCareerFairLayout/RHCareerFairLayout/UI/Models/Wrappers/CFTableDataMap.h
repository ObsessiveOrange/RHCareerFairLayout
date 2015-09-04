//
//  CFTableDataMap.h
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/10/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <FMDB.h>

@interface CFTableDataMap : NSObject

@property (strong, nonatomic) NSMutableDictionary* tableDict;

- (id) initWithTableData: (FMResultSet*) data;

- (void)removeObjectForKey:(id)aKey;
- (void)setObject:(id)anObject forKey:(id <NSCopying>)aKey;
- (id)objectForKey:(id)aKey;
- (NSEnumerator *)keyEnumerator;

@end
