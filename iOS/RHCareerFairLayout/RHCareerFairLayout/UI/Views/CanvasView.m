//
//  CanvasView.m
//  DrawingCanvas
//
//  Created by Benedict Seng Sum Wong on 4/24/15.
//  Copyright (c) 2015 Rose-Hulman. All rights reserved.
//

#import "CanvasView.h"
#import "UIColorExtensions.h"
#import "CFTableMappingArray.h"
#import "CFTableData.h"
#import "CFTerm.h"
#import "DBManager.h"
#import "RHCareerFairLayout-Swift.h"

static NSString * const BORDER_WIDTH_PREF_KEY = @"borderWidth";
static NSString * const CORNER_ROUNDING_PREF_KEY = @"cornerRounding";
static NSString * const SHAPE_COLOR_PREF_KEY = @"shapeColor";

@interface CanvasView ()

@property (nonatomic, strong) UITapGestureRecognizer *tapGestureRecognizer;
@property (nonatomic, strong) UIPanGestureRecognizer *panGestureRecognizer;
@property (nonatomic, strong) UIPinchGestureRecognizer *zoomGestureRecognizer;
@property (nonatomic) NSInteger containerWidth;
@property (nonatomic) NSInteger containerHeight;
@property (nonatomic) NSInteger mapWidth;
@property (nonatomic) NSInteger mapHeight;
@property (nonatomic) double fontSize;

@property (nonatomic, strong) CFTableData* mapAreaRect;
@property (nonatomic, strong) CFTableData* restAreaRect;
@property (nonatomic, strong) CFTableData* registrationAreaRect;
@property (nonatomic, strong) CFTableDataMap* tables;
@property (nonatomic) double focusX;
@property (nonatomic) double focusY;
@property (nonatomic) double centerX;
@property (nonatomic) double centerY;
@property (nonatomic) double scaleFactor;
@property (nonatomic) bool isSetup;

@end

@implementation CanvasView

- (void) commonInit{
    
    self.tapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTap:)];
    [self addGestureRecognizer:self.tapGestureRecognizer];
    
    self.panGestureRecognizer = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handlePan:)];
    [self addGestureRecognizer:self.panGestureRecognizer];
    
    self.zoomGestureRecognizer = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(handleZoom:)];
    [self addGestureRecognizer:self.zoomGestureRecognizer];
    
    self.scaleFactor = 1;
    self.isSetup = false;
    
    [[NSUserDefaults standardUserDefaults] registerDefaults:@{BORDER_WIDTH_PREF_KEY:@2.0f, CORNER_ROUNDING_PREF_KEY:@0.0f, SHAPE_COLOR_PREF_KEY:@"#000000"}];
}

- (id)initWithFrame:(CGRect)frame;
{
    if ((self = [super initWithFrame:frame])) {
        [self commonInit];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder;
{
    if ((self = [super initWithCoder:aDecoder])) {
        [self commonInit];
    }
    return self;
}

- (void) setupView{
    self.isSetup = true;
    
    float focusX = 0, focusY = 0, scaleFactor = 1;
    
    // portrait
    if(self.frame.size.height > self.frame.size.width){
        focusX = [[NSUserDefaults standardUserDefaults] floatForKey:@"focusX-port"];
        focusY = [[NSUserDefaults standardUserDefaults] floatForKey:@"focusY-port"];
        scaleFactor = [[NSUserDefaults standardUserDefaults] floatForKey:@"scaleFactor-port"];
    }
    // landscape
    else{
        focusX = [[NSUserDefaults standardUserDefaults] floatForKey:@"focusX-land"];
        focusY = [[NSUserDefaults standardUserDefaults] floatForKey:@"focusY-land"];
        scaleFactor = [[NSUserDefaults standardUserDefaults] floatForKey:@"scaleFactor-land"];
    }
    
    self.focusX = focusX != 0 ? focusX : self.frame.size.width / 2.0;
    self.focusY = focusY != 0 ? focusY : self.frame.size.height / 2.0;
    self.scaleFactor = scaleFactor != 0 ? scaleFactor : 1.0;
    
    self.centerX = self.frame.size.width / 2.0;
    self.centerY = self.frame.size.height / 2.0;
    
    [self generateTableLocations];
    
}

- (void) drawRect:(CGRect)rect{
    if(self.isSetup){
        
        NSArray* tables = [self.tables.tableDict allValues];
        
        
        CGContextRef context = UIGraphicsGetCurrentContext();
        
        
        [self drawTableOnContext:context withData:self.mapAreaRect];
        [self drawTableOnContext:context withData:self.restAreaRect];
        [self drawTableOnContext:context withData:self.registrationAreaRect];
        
        for(int i = 0; i < [tables count]; i++){
            
            CFTableData* table = tables[i];
            
            [self drawTableOnContext:context withData:table];
        }
    }
}

- (void) drawTableOnContext:(CGContextRef) context withData: (CFTableData*) tableData{
    
    CGRect offsetRect = CGRectMake(self.focusX + ((tableData.rectangle.rect.origin.x - self.focusX) + (self.focusX - self.centerX)) * self.scaleFactor,
                                   self.focusY + ((tableData.rectangle.rect.origin.y - self.focusY) + (self.focusY - self.centerY)) * self.scaleFactor,
                                   tableData.rectangle.rect.size.width * self.scaleFactor,
                                   tableData.rectangle.rect.size.height * self.scaleFactor);
    
    //draw table outline and background
    CGPathRef path = CGPathCreateWithRect(offsetRect, NULL);
    
    if(self.highlightCompany && [self.highlightCompany integerValue] == tableData.tableMapping.id){
        [RHCareerFairLayout.color_yellow setFill];
    }
    else{
        [(tableData.rectangle.fillColor == nil ? [UIColor whiteColor] : tableData.rectangle.fillColor) setFill];
    }
    [(tableData.rectangle.lineColor == nil ? [UIColor whiteColor] : tableData.rectangle.lineColor) setStroke];
    CGContextSetLineWidth(context, 0.5f);
    CGContextAddPath(context, path);
    CGContextDrawPath(context, kCGPathFillStroke);
    CGPathRelease(path);
    
    //draw text
    if(tableData.rectangle.text != nil){
        //Draw Text
        NSMutableParagraphStyle* textStyle = NSMutableParagraphStyle.defaultParagraphStyle.mutableCopy;
        textStyle.alignment = NSTextAlignmentCenter;
        
        CGFloat yOffset = (offsetRect.size.height - self.fontSize * self.scaleFactor) / 2.0 - self.fontSize * self.scaleFactor / 8.0;
        CGRect textRect = CGRectMake(offsetRect.origin.x, offsetRect.origin.y + yOffset, offsetRect.size.width, self.fontSize * self.scaleFactor);
        
        NSDictionary* textFontAttributes = @{NSFontAttributeName: [UIFont systemFontOfSize: self.fontSize * self.scaleFactor], NSParagraphStyleAttributeName: textStyle};
        
        [tableData.rectangle.text drawInRect: textRect withAttributes: textFontAttributes];
    }
}

- (void)handleTap: (UITapGestureRecognizer*) gestureRecognizer{
    
    NSArray* tables = self.tables.tableDict.allValues;
    
    CGPoint location = [gestureRecognizer locationInView:self];
    location.x = self.centerX + (location.x - self.focusX) / self.scaleFactor;
    location.y = self.centerY + (location.y - self.focusY) / self.scaleFactor;
    
    for(int i = 0; i < [tables count]; i++){
        CFTableData* tableData = tables[i];
        if(tableData.rectangle.tappable){
            if(CGRectContainsPoint(tableData.rectangle.rect, location)){
                
                NSInteger companyId = [tableData.tableMapping.tableMapping_companyId integerValue];
                self.layoutViewController.selectedCompany = [DBManager getCompanyWithId:companyId];
                [self.layoutViewController performSegueWithIdentifier:RHCareerFairLayout.companyDetailSegueIdentifier sender:self];
            }
        }
    }
    
    //    CGPoint location = [gestureRecognizer locationInView: self];
    //    UIView* subview = [self hitTest:location withEvent:nil];
    //
    //    //If no subview there, add a new one
    //    if(![self.subviews containsObject:subview]){
    //
    //        //create a new box centered at tap location, of size 50/50
    //        UIView *newBox  = [[UIView alloc] initWithFrame:CGRectMake(location.x - 25, location.y - 25, 50, 50)];
    //
    //        //set background color and corner radius based on user settings
    //        newBox.backgroundColor = [UIColor colorwithHexString:@"#000000" alpha:1.0];
    ////        newBox.layer.cornerRadius = [[NSUserDefaults standardUserDefaults] doubleForKey:CORNER_ROUNDING_PREF_KEY];
    //
    //        //add to superview
    //        [self addSubview:newBox];
    //    }
    //
    //    //Otherwise, toggle subview selected.
    //    else{
    //        //if currently selected, remove from selected list, remove border
    //        if([self.selectedSubviews containsObject:subview]){
    //            [self.selectedSubviews removeObject:subview];
    //            subview.layer.borderWidth = 0.0;
    //        }
    //        //if not currently selected, select it, add border.
    //        else{
    //            [self.selectedSubviews addObject:subview];
    //            subview.layer.borderWidth = [[NSUserDefaults standardUserDefaults] doubleForKey:BORDER_WIDTH_PREF_KEY];
    //            subview.layer.borderColor = [[UIColor redColor] CGColor];
    //        }
    //    }
}

- (void)handlePan: (UIPanGestureRecognizer*) gestureRecognizer{
    
    //pan canvas on 1 finger touch.
    //    if(gestureRecognizer.numberOfTouches == 1){
    //
    //        CGFloat newX = self.frame.origin.x ;
    //        CGFloat newY = self.frame.origin.y ;
    //
    //        //set new frame on each object based on the changes reflected in gestureRecognizer
    //        if(self.frame.origin.x >= self.superview.frame.size.width - self.frame.size.width && self.frame.origin.x <= 0){
    //            newX = fmin(fmax(self.frame.origin.x + [gestureRecognizer translationInView:self].x, self.superview.frame.size.width - self.frame.size.width), 0);
    //        }
    //        if(self.frame.origin.y >= self.superview.frame.size.height - self.frame.size.height && self.frame.origin.y <= 0){
    //            newY = fmin(fmax(self.frame.origin.y + [gestureRecognizer translationInView:self].y, self.superview.frame.size.height - self.frame.size.height), 0);
    //        }
    //        [self setFrame:CGRectMake(newX, newY, self.frame.size.width, self.frame.size.height)];
    //        [gestureRecognizer setTranslation:CGPointMake(0, 0) inView:self];
    //    }
    //
    //    //ignore everything but 2 finger touches.
    //    if(gestureRecognizer.numberOfTouches == 2){
    
    //set new frame on each object based on the changes reflected in gestureRecognizer
    //    for(UIView* subview in self.selectedSubviews){
    //        [subview setFrame:CGRectMake(subview.frame.origin.x + [gestureRecognizer translationInView:self].x, subview.frame.origin.y + [gestureRecognizer translationInView:self].y, 50, 50)];
    //    }
    
    self.focusX = [self getNewXWithDelta: [gestureRecognizer translationInView:self].x];
    self.focusY = [self getNewYWithDelta: [gestureRecognizer translationInView:self].y];
    
    NSLog(@"Pan to:(%f,%f)", self.focusX, self.focusY);
    
    
    if(self.frame.size.height > self.frame.size.width){
        [[NSUserDefaults standardUserDefaults] setFloat:self.focusX forKey:@"focusX-port"];
        [[NSUserDefaults standardUserDefaults] setFloat:self.focusY forKey:@"focusY-port"];
    }
    // landscape
    else{
        [[NSUserDefaults standardUserDefaults] setFloat:self.focusX forKey:@"focusX-land"];
        [[NSUserDefaults standardUserDefaults] setFloat:self.focusY forKey:@"focusY-land"];
    }
    
    [self setNeedsDisplay];
    
    //reset gestureRecognizer's translation
    [gestureRecognizer setTranslation:CGPointMake(0, 0) inView:self];
    //    }
}


- (void)handleZoom: (UIPinchGestureRecognizer*) gestureRecognizer{
    self.scaleFactor = fmin(fmax(self.scaleFactor + gestureRecognizer.scale - 1, 1), 5);
    
    NSLog(@"Scale %f to:%f", gestureRecognizer.scale-1, self.scaleFactor);
    
    [gestureRecognizer setScale:1.0];
    self.focusX = [self getNewXWithDelta:0];
    self.focusY = [self getNewYWithDelta:0];
    
    if(self.frame.size.height > self.frame.size.width){
        [[NSUserDefaults standardUserDefaults] setFloat:self.scaleFactor forKey:@"scaleFactor-port"];
    }
    // landscape
    else{
        [[NSUserDefaults standardUserDefaults] setFloat:self.scaleFactor forKey:@"scaleFactor-land"];
    }
    
    [self setNeedsDisplay];
}

- (double) getNewXWithDelta: (double) delta{
    if(self.mapWidth * self.scaleFactor >= self.containerWidth){
        return fmax(
                    fmin(
                         self.focusX + delta,
                         self.mapWidth * self.scaleFactor/2.0
                         ),
                    self.containerWidth-self.mapWidth * self.scaleFactor/2.0
                    );
    }
    else{
        return fmin(
                    fmin(
                         self.focusX + delta,
                         self.mapWidth * self.scaleFactor/2.0
                         ),
                    self.containerWidth-self.mapWidth * self.scaleFactor/2.0
                    );
    }
}

- (double) getNewYWithDelta: (double) delta{
    
    
    if(self.mapHeight * self.scaleFactor >= self.containerHeight){
        return fmax(
                    fmin(
                         self.focusY + delta,
                         self.mapHeight * self.scaleFactor/2.0
                         ),
                    self.containerHeight-self.mapHeight * self.scaleFactor/2.0
                    );
    }
    else{
        return fmin(
                    fmax(
                         self.focusY + delta,
                         self.mapHeight * self.scaleFactor/2.0
                         ),
                    self.containerHeight-self.mapHeight * self.scaleFactor/2.0
                    );
    }
}

-(void) generateTableLocations{
    
    self.containerWidth = self.frame.size.width;
    self.containerHeight = self.frame.size.height;
    //
    // Calculate usable width
    self.mapWidth = self.containerWidth >= self.containerHeight * 2.0 ? self.containerHeight * 2.0 : self.containerWidth;
    self.mapHeight = self.containerWidth >= self.containerHeight * 2.0 ? self.containerHeight : self.containerWidth / 2.0;
    
    self.tables = [DBManager getTables];
    CFTerm* term = [DBManager getTerm];
    
    //convenience assignments
    NSInteger s1 = term.term_layout_Section1;
    NSInteger s2 = term.term_layout_Section2;
    NSInteger s2Rows = term.term_layout_Section2_Rows;
    NSInteger s2PathWidth = term.term_layout_Section2_PathWidth;
    NSInteger s3 = term.term_layout_Section3;
    
    //count number of vertical and horizontal mTableMap there are
    NSInteger hrzCount = s2 + fmin(s1, 1.0) + fmin(s3, 1.0);
    NSInteger vrtCount = fmax(s1, s3);
    
    //calculate width and height of mTableMap based on width of the canvas
    double unitX = self.mapWidth / 100.0;
    
    //10 + (number of sections - 1) * 5 % of space allocated to (vertical) walkways
    double tableWidth = unitX * (90.0 - fmin(s1, 1.0) * 5.0 - fmin(s3, 1.0) * 5.0) / hrzCount;
    double unitY = self.mapHeight / 100.0;
    
    //30% of space allocated to registration and rest area.
    double tableHeight = unitY * 70.0 / vrtCount;
    self.fontSize = tableHeight * 2.0 / 3.0;
    
    //
    NSInteger tableId = 1;
    double offsetX = (self.containerWidth - self.mapWidth) / 2.0;
    double offsetY = (self.containerHeight - self.mapHeight) / 2.0;
    
    
    // static tables.
    self.mapAreaRect = [[CFTableData alloc] initWithTableMapping:nil
                                                   withRectangle:[[CFRectangle alloc]
                                                                  initWithRect:CGRectMake(offsetX,
                                                                                          offsetY,
                                                                                          self.mapWidth,
                                                                                          self.mapHeight)
                                                                  withLineColor:RHCareerFairLayout.color_black
                                                                  withFillColor:nil
                                                                  withText:nil
                                                                  tappable:false]
                                                        selected:false];
    
    self.restAreaRect = [[CFTableData alloc] initWithTableMapping:nil
                                                    withRectangle:[[CFRectangle alloc]
                                                                   initWithRect:CGRectMake(offsetX + 40 * unitX,
                                                                                           offsetY + 80 * unitY,
                                                                                           45 * unitX,
                                                                                           15 * unitY)
                                                                   withLineColor:RHCareerFairLayout.color_black
                                                                   withFillColor:nil
                                                                   withText:@"Rest Area"
                                                                   tappable:false]
                                                         selected:false];
    
    self.registrationAreaRect = [[CFTableData alloc] initWithTableMapping:nil
                                                            withRectangle:[[CFRectangle alloc]
                                                                           initWithRect:CGRectMake(offsetX + 5 * unitX,
                                                                                                   offsetY + 80 * unitY,
                                                                                                   30 * unitX,
                                                                                                   15 * unitY)
                                                                           withLineColor:RHCareerFairLayout.color_black
                                                                           withFillColor:nil
                                                                           withText:@"Registration"
                                                                           tappable:false]
                                                                 selected:false];
    
    
    //
    // section 1
    offsetX += 5 * unitX;
    if (s1 > 0) {
        for (int i = 0; i < s1; ) {
            CFTableData* table = [self.tables objectForKey:@(tableId)];
            
            CFRectangle* rectangle = [[CFRectangle alloc]
                                      initWithRect:CGRectMake(offsetX,
                                                              offsetY + 5 * unitY + i * tableHeight,
                                                              tableWidth,
                                                              tableHeight * table.tableMapping.tableMapping_size)
                                      withLineColor:RHCareerFairLayout.color_black
                                      withFillColor:table.selected ? RHCareerFairLayout.color_green : nil
                                      withText:[[NSString alloc] initWithFormat:@"%ld", (long)table.tableMapping.id]
                                      tappable:table.selected];
            table.rectangle = rectangle;
            
            tableId++;
            i += table.tableMapping.tableMapping_size;
        }
        offsetX += tableWidth + 5 * unitX;
    }
    //
    // section 2
    double pathWidth = (unitY * 70 - s2Rows * tableHeight) / (s2Rows / 2);
    //
    //rows
    if (s2Rows > 0 && s2 > 0) {
        for (int i = 0; i < s2Rows; i++) {
            //
            //Outer rows have no walkway.
            //Also use this if there is no path inbetween the left and right.
            if (s2PathWidth == 0 || i == 0 || i == s2Rows - 1) {
                for (int j = 0; j < s2; ) {
                    CFTableData* table = [self.tables objectForKey:@(tableId)];
                    
                    CFRectangle* rectangle = [[CFRectangle alloc]
                                              initWithRect:CGRectMake(offsetX + (j * tableWidth),
                                                                      offsetY + 5 * unitY + floor((i + 1) / 2) * pathWidth + i * tableHeight,
                                                                      tableWidth * table.tableMapping.tableMapping_size,
                                                                      tableHeight)
                                              withLineColor:RHCareerFairLayout.color_black
                                              withFillColor:table.selected ? RHCareerFairLayout.color_green : nil
                                              withText:[[NSString alloc] initWithFormat:@"%ld", (long)table.tableMapping.id]
                                              tappable:table.selected];
                    
                    table.rectangle = rectangle;
                    
                    tableId++;
                    j += table.tableMapping.tableMapping_size;
                }
            }
            //
            //inner rows need to have walkway halfway through
            else {
                NSInteger leftTables = ((s2 - s2PathWidth) / 2);
                NSInteger rightTables = s2 - s2PathWidth - leftTables;
                for (int j = 0; j < leftTables; ) {
                    CFTableData* table = [self.tables objectForKey:@(tableId)];
                    
                    CFRectangle* rectangle = [[CFRectangle alloc]
                                              initWithRect:CGRectMake(offsetX + (j * tableWidth),
                                                                      offsetY + 5 * unitY + floor((i + 1) / 2) * pathWidth + i * tableHeight,
                                                                      tableWidth * table.tableMapping.tableMapping_size,
                                                                      tableHeight)
                                              withLineColor:RHCareerFairLayout.color_black
                                              withFillColor:table.selected ? RHCareerFairLayout.color_green : nil
                                              withText:[[NSString alloc] initWithFormat:@"%ld", (long)table.tableMapping.id]
                                              tappable:table.selected];
                    
                    table.rectangle = rectangle;
                    
                    tableId++;
                    j += table.tableMapping.tableMapping_size;
                }
                for (int j = 0; j < rightTables; ) {
                    CFTableData* table = [self.tables objectForKey:@(tableId)];
                    
                    CFRectangle* rectangle = [[CFRectangle alloc]
                                              initWithRect:CGRectMake(offsetX + ((leftTables + s2PathWidth + j) * tableWidth),
                                                                      offsetY + 5 * unitY + floor((i + 1) / 2) * pathWidth + i * tableHeight,
                                                                      tableWidth * table.tableMapping.tableMapping_size,
                                                                      tableHeight)
                                              withLineColor:RHCareerFairLayout.color_black
                                              withFillColor:table.selected ? RHCareerFairLayout.color_green : nil
                                              withText:[[NSString alloc] initWithFormat:@"%ld", (long)table.tableMapping.id]
                                              tappable:table.selected];
                    
                    table.rectangle = rectangle;
                    
                    tableId++;
                    j += table.tableMapping.tableMapping_size;
                }
            }
        }
        offsetX += s2 * tableWidth + 5 * unitX;
    }
    //
    // section 3
    if (s3 > 0) {
        for (int i = 0; i < s3; ) {
            CFTableData* table = [self.tables objectForKey:@(tableId)];
            
            CFRectangle* rectangle = [[CFRectangle alloc]
                                      initWithRect:CGRectMake(offsetX,
                                                              offsetY + 5 * unitY + i * tableHeight,
                                                              tableWidth,
                                                              tableHeight * table.tableMapping.tableMapping_size)
                                      withLineColor:RHCareerFairLayout.color_black
                                      withFillColor:table.selected ? RHCareerFairLayout.color_green : nil
                                      withText:[[NSString alloc] initWithFormat:@"%ld", (long)table.tableMapping.id]
                                      tappable:table.selected];
            table.rectangle = rectangle;
            
            tableId++;
            i += table.tableMapping.tableMapping_size;
        }
    }
    offsetX += tableWidth + 5 * unitX;
    
    NSArray* allKeys = [self.tables.tableDict allKeys];
    for(int i = 0; i < [allKeys count]; i++){
        if([(NSNumber*)allKeys[i] integerValue] >= tableId){
            [self.tables removeObjectForKey:allKeys[i]];
        }
    }
}

- (void) drawTables{
    
    NSArray* tables = [self.tables.tableDict allValues];
    
    for(int i = 0; i < [tables count]; i++){
        
        CFTableData* table = tables[i];
        
        UIView *newBox  = [[UIView alloc] initWithFrame:table.rectangle.rect];
        
        //set background color and corner radius based on user settings
        //            newBox.backgroundColor = table.rectangle;
        //        newBox.layer.cornerRadius = [[NSUserDefaults standardUserDefaults] doubleForKey:CORNER_ROUNDING_PREF_KEY];
        
        //add to superview
        [self addSubview:newBox];
    }
}


@end
