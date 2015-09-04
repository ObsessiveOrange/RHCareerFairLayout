//
//  DBStatements.swift
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/6/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

import UIKit

class DBStatements: NSObject {
    
    //
    // Filename constant
    static let DATABASE_NAME = "RHCareerFairLayout.db";
    //
    // Table name constants
    static let TABLE_CATEGORY_NAME : String = "Category";
    static let TABLE_COMPANY_NAME : String = "Company";
    static let TABLE_COMPANYCATEGORY_NAME : String = "Company_Category";
    static let TABLE_TABLEMAPPING_NAME : String = "TableMapping";
    static let TABLE_TERM_NAME : String = "Term";
    static let TABLE_SELECTED_COMPANIES_NAME : String = "SelectedCompanies";
    static let TABLE_SELECTED_CATEGORIES_NAME : String = "SelectedCategories";
    //
    // View name constants
    static let VIEW_FILTERED_COMPANIES_NAME : String = "FilteredCompanies";
    static let VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME : String = "FilteredCompaniesByMajor";
    static let VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION_NAME : String = "FilteredCompaniesByWorkAuthorization";
    static let VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE_NAME : String = "FilteredCompaniesByPositionType";
    
    //
    // Column key constants - see documentation for more
    static let KEY_PRIMARY_ID : String = "_id";
    static let KEY_ID : String = "id";
    static let KEY_NAME : String = "name";
    static let KEY_TYPE : String = "type";
    static let KEY_DESCRIPTION : String = "description";
    static let KEY_WEBSITE_LINK : String = "websiteLink";
    static let KEY_ADDRESS : String = "address";
    static let KEY_TABLE : String = "tableId";
    static let KEY_COMPANY_ID : String = "companyId";
    static let KEY_CATEGORY_ID : String = "categoryId";
    static let KEY_SIZE : String = "size";
    static let KEY_YEAR : String = "year";
    static let KEY_QUARTER : String = "quarter";
    static let KEY_LAYOUT_SECTION1 : String = "layout_Section1";
    static let KEY_LAYOUT_SECTION2 : String = "layout_Section2";
    static let KEY_LAYOUT_SECTION2_PATHWIDTH : String = "layout_Section2_PathWidth";
    static let KEY_LAYOUT_SECTION2_ROWS : String = "layout_Section2_Rows";
    static let KEY_LAYOUT_SECTION3 : String = "layout_Section3";
    static let KEY_LAST_UPDATE_TIME : String = "lastUpdateTime";
    static let KEY_SELECTED : String = "selected";
    
    //
    // Database table & view creation statements.
    static let CREATE_TABLE_CATEGORY : String =
    "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORY_NAME + " (" +
        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        KEY_ID + " INTEGER NOT NULL, " +
        KEY_NAME + " VARCHAR(100) NOT NULL, " +
        KEY_TYPE + " VARCHAR(50) NOT NULL, " +
        "UNIQUE (" + KEY_ID + "), " +
        "UNIQUE(" + KEY_NAME + ", " + KEY_TYPE + ") " +
    ");";
    
    static let CREATE_TABLE_COMPANY : String =
    "CREATE TABLE IF NOT EXISTS " + TABLE_COMPANY_NAME + " (" +
        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        KEY_ID + " INTEGER NOT NULL, " +
        KEY_NAME + " VARCHAR(100) NOT NULL, " +
        KEY_DESCRIPTION + " TEXT NOT NULL, " +
        KEY_WEBSITE_LINK + " VARCHAR(100) NOT NULL, " +
        KEY_ADDRESS + " VARCHAR(250) NOT NULL, " +
        "UNIQUE (" + KEY_ID + ") " +
    ");";
    
    static let CREATE_TABLE_COMPANYCATEGORY : String =
    "CREATE TABLE IF NOT EXISTS " + TABLE_COMPANYCATEGORY_NAME + " (" +
        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        KEY_COMPANY_ID + " INTEGER NOT NULL, " +
        KEY_CATEGORY_ID + " INTEGER NOT NULL, " +
        "UNIQUE (" + KEY_COMPANY_ID + ", " + KEY_CATEGORY_ID + "), " +
        "FOREIGN KEY (" + KEY_COMPANY_ID + ") REFERENCES " + TABLE_COMPANY_NAME + "(" + KEY_ID + ") ON UPDATE CASCADE ON DELETE CASCADE, " +
        "FOREIGN KEY (" + KEY_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY_NAME + "(" + KEY_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
    ");";
    
    static let CREATE_TABLE_TABLEMAPPING : String =
    "CREATE TABLE IF NOT EXISTS " + TABLE_TABLEMAPPING_NAME + " (" +
        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        KEY_ID + " INTEGER NOT NULL, " +
        KEY_COMPANY_ID + " INTEGER, " +
        KEY_SIZE + " INTEGER NOT NULL, " +
        "UNIQUE (" + KEY_ID + "), " +
        "FOREIGN KEY (" + KEY_COMPANY_ID + ") REFERENCES " + TABLE_COMPANY_NAME + "(" + KEY_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
    ");";
    
    
    static let CREATE_TABLE_TERM : String =
    "CREATE TABLE IF NOT EXISTS " + TABLE_TERM_NAME + " (" +
        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        KEY_YEAR + " INTEGER NOT NULL, " +
        KEY_QUARTER + " VARCHAR(10) NOT NULL, " +
        KEY_LAYOUT_SECTION1 + " INTEGER NOT NULL, " +
        KEY_LAYOUT_SECTION2 + " INTEGER NOT NULL, " +
        KEY_LAYOUT_SECTION2_PATHWIDTH + " INTEGER NOT NULL, " +
        KEY_LAYOUT_SECTION2_ROWS + " INTEGER NOT NULL, " +
        KEY_LAYOUT_SECTION3 + " INTEGER NOT NULL, " +
        KEY_LAST_UPDATE_TIME + " FLOAT NOT NULL, " +
        "UNIQUE (" + KEY_YEAR + ", " + KEY_QUARTER + ") " +
    ");";
    
    static let CREATE_TABLE_SELECTED_CATEGORIES : String =
    "CREATE TABLE IF NOT EXISTS " + TABLE_SELECTED_CATEGORIES_NAME + " (" +
        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        KEY_CATEGORY_ID + " INTEGER NOT NULL, " +
        KEY_SELECTED + " BOOLEAN NOT NULL DEFAULT 0, " +
        "UNIQUE (" + KEY_CATEGORY_ID + "), " +
        "FOREIGN KEY (" + KEY_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY_NAME + "(" + KEY_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
    ");";
    
    static let CREATE_TABLE_SELECTED_COMPANIES : String =
    "CREATE TABLE IF NOT EXISTS " + TABLE_SELECTED_COMPANIES_NAME + " (" +
        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        KEY_COMPANY_ID + " INTEGER NOT NULL, " +
        KEY_SELECTED + " BOOLEAN NOT NULL DEFAULT 0, " +
        "UNIQUE (" + KEY_COMPANY_ID + "), " +
        "FOREIGN KEY (" + KEY_COMPANY_ID + ") REFERENCES " + TABLE_COMPANY_NAME + "(" + KEY_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
    ");";
    
    static let CREATE_VIEW_FILTERED_COMPANIES: String =
    "CREATE VIEW IF NOT EXISTS " + VIEW_FILTERED_COMPANIES_NAME + " AS" +
        " SELECT " + VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME + "." + KEY_COMPANY_ID + " AS " + KEY_COMPANY_ID +
        " FROM " + VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME + " JOIN " +
        VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE_NAME + " ON " +
        VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME + "." + KEY_COMPANY_ID + " = " +
        VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE_NAME + "." + KEY_COMPANY_ID + " JOIN " +
        VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION_NAME + " ON " +
        VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME + "." + KEY_COMPANY_ID + " = " +
        VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION_NAME + "." + KEY_COMPANY_ID + ";"
    
    static let CREATE_VIEW_FILTERED_COMPANIES_BY_MAJOR : String =
    "CREATE VIEW IF NOT EXISTS " + VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME + " AS" +
        " SELECT DISTINCT " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_COMPANY_ID +
        " FROM " + TABLE_COMPANYCATEGORY_NAME + " JOIN " + TABLE_SELECTED_CATEGORIES_NAME +
        " ON " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " = " +
        TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID +
        " JOIN " + TABLE_CATEGORY_NAME +
        " ON " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " = " +
        TABLE_CATEGORY_NAME + "." + KEY_ID +
        " WHERE ((NOT EXISTS(SELECT 1" +
        " FROM " + TABLE_SELECTED_CATEGORIES_NAME + " JOIN " + TABLE_CATEGORY_NAME +
        " ON " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID + " = " +
        TABLE_CATEGORY_NAME + "." + KEY_ID +
        " WHERE type = 'Major' AND " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_SELECTED + " = 1))" +
        " OR (" + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " IN(" +
        " SELECT " + TABLE_CATEGORY_NAME + "." + KEY_ID +
        " FROM " + TABLE_SELECTED_CATEGORIES_NAME + " JOIN " + TABLE_CATEGORY_NAME +
        " ON " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID + " = " +
        TABLE_CATEGORY_NAME + "." + KEY_ID +
        " WHERE type = 'Major' AND " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_SELECTED + " = 1)));"
    
    
    static let CREATE_VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION : String =
    "CREATE VIEW IF NOT EXISTS " + VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION_NAME + " AS" +
        " SELECT DISTINCT " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_COMPANY_ID +
        " FROM " + TABLE_COMPANYCATEGORY_NAME + " JOIN " + TABLE_SELECTED_CATEGORIES_NAME +
        " ON " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " = " +
        TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID +
        " JOIN " + TABLE_CATEGORY_NAME +
        " ON " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " = " +
        TABLE_CATEGORY_NAME + "." + KEY_ID +
        " WHERE ((NOT EXISTS(SELECT 1" +
        " FROM " + TABLE_SELECTED_CATEGORIES_NAME + " JOIN " + TABLE_CATEGORY_NAME +
        " ON " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID + " = " +
        TABLE_CATEGORY_NAME + "." + KEY_ID +
        " WHERE type = 'Work Authorization' AND " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_SELECTED + " = 1))" +
        " OR (" + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " IN(" +
        " SELECT " + TABLE_CATEGORY_NAME + "." + KEY_ID +
        " FROM " + TABLE_SELECTED_CATEGORIES_NAME + " JOIN " + TABLE_CATEGORY_NAME +
        " ON " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID + " = " +
        TABLE_CATEGORY_NAME + "." + KEY_ID +
        " WHERE type = 'Work Authorization' AND " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_SELECTED + " = 1)));"
    
    
    static let CREATE_VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE : String =
    "CREATE VIEW IF NOT EXISTS " + VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE_NAME + " AS" +
        " SELECT DISTINCT " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_COMPANY_ID +
        " FROM " + TABLE_COMPANYCATEGORY_NAME + " JOIN " + TABLE_SELECTED_CATEGORIES_NAME +
        " ON " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " = " +
        TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID +
        " JOIN " + TABLE_CATEGORY_NAME +
        " ON " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " = " +
        TABLE_CATEGORY_NAME + "." + KEY_ID +
        " WHERE ((NOT EXISTS(SELECT 1" +
        " FROM " + TABLE_SELECTED_CATEGORIES_NAME + " JOIN " + TABLE_CATEGORY_NAME +
        " ON " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID + " = " +
        TABLE_CATEGORY_NAME + "." + KEY_ID +
        " WHERE type = 'Position Type' AND " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_SELECTED + " = 1))" +
        " OR (" + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " IN(" +
        " SELECT " + TABLE_CATEGORY_NAME + "." + KEY_ID +
        " FROM " + TABLE_SELECTED_CATEGORIES_NAME + " JOIN " + TABLE_CATEGORY_NAME +
        " ON " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID + " = " +
        TABLE_CATEGORY_NAME + "." + KEY_ID +
        " WHERE type = 'Position Type' AND " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_SELECTED + " = 1)));"
    
    //
    // Database drop statements
    static let DROP_TABLE_CATEGORY : String = "DROP TABLE IF EXISTS " + TABLE_CATEGORY_NAME;
    static let DROP_TABLE_COMPANY : String = "DROP TABLE IF EXISTS " + TABLE_COMPANY_NAME;
    static let DROP_TABLE_COMPANYCATEGORY : String = "DROP TABLE IF EXISTS " + TABLE_COMPANYCATEGORY_NAME;
    static let DROP_TABLE_TABLEMAPPING : String = "DROP TABLE IF EXISTS " + TABLE_TABLEMAPPING_NAME;
    static let DROP_TABLE_TERM : String = "DROP TABLE IF EXISTS " + TABLE_TERM_NAME;
    static let DROP_TABLE_SELECTED_CATEGORIES : String = "DROP TABLE IF EXISTS " + TABLE_SELECTED_CATEGORIES_NAME;
    static let DROP_TABLE_SELECTED_COMPANIES : String = "DROP TABLE IF EXISTS " + TABLE_SELECTED_COMPANIES_NAME;
    static let DROP_VIEW_FILTERED_COMPANIES : String = "DROP VIEW IF EXISTS " + VIEW_FILTERED_COMPANIES_NAME;
    static let DROP_VIEW_FILTERED_COMPANIES_BY_MAJOR : String = "DROP VIEW IF EXISTS " + VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME;
    static let DROP_VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE : String = "DROP VIEW IF EXISTS " + VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE_NAME;
    static let DROP_VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION : String = "DROP VIEW IF EXISTS " + VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION_NAME;
    
    // 
    // Often-used utility statements.
    static let JOIN_STATEMENT : String = "%@ JOIN %@ ON %@.%@ = %@.%@";
    static let LEFT_OUTER_JOIN_STATEMENT : String = "%@ LEFT OUTER JOIN %@ ON %@.%@ = %@.%@";
    
}
