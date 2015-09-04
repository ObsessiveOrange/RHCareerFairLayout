//
//  CategoryDict.h
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CFCategory.h"

@interface CFCategoryDict : NSObject

@property (strong, nonatomic) NSMutableDictionary* categoryDict;

- (id) initWithDictionary:(NSDictionary *) data;

- (void)removeObjectForKey:(id)aKey;
- (void)setObject:(id)anObject forKey:(id <NSCopying>)aKey;
- (id)objectForKey:(id)aKey;
- (id)objectForIntKey:(NSInteger)aKey;
- (NSEnumerator *)keyEnumerator;

@end
