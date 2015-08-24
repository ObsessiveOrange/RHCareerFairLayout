package cf.obsessiveorange.rhcareerfairlayout.data.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Category;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Company;
import cf.obsessiveorange.rhcareerfairlayout.data.models.TableMapping;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Term;
import cf.obsessiveorange.rhcareerfairlayout.data.models.wrappers.CompanyMap;
import cf.obsessiveorange.rhcareerfairlayout.data.models.wrappers.DataWrapper;
import cf.obsessiveorange.rhcareerfairlayout.data.models.wrappers.TableMappingArray;
import cf.obsessiveorange.rhcareerfairlayout.ui.models.wrappers.TableMap;

public class DBManager {

    // DB Schema version constant
    private static final int DATABASE_VERSION = 2;

    // Filename constant
    private static final String DATABASE_NAME = "RHCareerFairLayout.db";

    // Table name constants
    private static final String TABLE_CATEGORY_NAME = "Category";
    private static final String TABLE_COMPANY_NAME = "Company";
    private static final String TABLE_COMPANYCATEGORY_NAME = "Company_Category";
    private static final String TABLE_TABLEMAPPING_NAME = "TableMapping";
    private static final String TABLE_TERM_NAME = "Term";
    private static final String TABLE_SELECTED_COMPANIES_NAME = "SelectedCompanies";
    private static final String TABLE_SELECTED_CATEGORIES_NAME = "SelectedCategories";

    // View name constants
    private static final String VIEW_FILTERED_COMPANIES_NAME = "FilteredCompanies";
    private static final String VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME = "FilteredCompaniesByMajor";
    private static final String VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION_NAME = "FilteredCompaniesByWorkAuthorization";
    private static final String VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE_NAME = "FilteredCompaniesByPositionType";

    // Column key constants - see documentation for more
    public static final String KEY_PRIMARY_ID = "_id";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_WEBSITE_LINK = "websiteLink";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_TABLE = "tableId";
    public static final String KEY_COMPANY_ID = "companyId";
    public static final String KEY_CATEGORY_ID = "categoryId";
    public static final String KEY_SIZE = "size";
    public static final String KEY_YEAR = "year";
    public static final String KEY_QUARTER = "quarter";
    public static final String KEY_LAYOUT_SECTION1 = "layout_Section1";
    public static final String KEY_LAYOUT_SECTION2 = "layout_Section2";
    public static final String KEY_LAYOUT_SECTION2_PATHWIDTH = "layout_Section2_PathWidth";
    public static final String KEY_LAYOUT_SECTION2_ROWS = "layout_Section2_Rows";
    public static final String KEY_LAYOUT_SECTION3 = "layout_Section3";
    public static final String KEY_LAST_UPDATE_TIME = "lastUpdateTime";
    public static final String KEY_SELECTED = "selected";

    // DB Helpers
    private static Context mContext = null;
    private static DBHelper mOpenHelper = null;
    private static SQLiteDatabase mDatabase = null;

    public static void setupDBAdapterIfNeeded(Context context) {
        mContext = context;

        // Create a SQLiteOpenHelper
        if (mOpenHelper == null) {
            mOpenHelper = new DBHelper(context);
        }
        // Open the database
        if (mDatabase == null) {
            mDatabase = mOpenHelper.getWritableDatabase();
        }
    }

    public static Long getLastUpdateTime() {

        String[] projection = new String[]{KEY_LAST_UPDATE_TIME};

        Cursor c = mDatabase.query(TABLE_TERM_NAME, projection, null, null, null, null, null);

        if (c.getCount() == 0) {
            return null;
        }

        c.moveToFirst();
        return c.getLong(c.getColumnIndexOrThrow(KEY_LAST_UPDATE_TIME));
    }

    /**
     * Load all new data contained in DataWrapper into DB.
     * Note that this mehtod deletes all previous data EXCEPT categories, and category selections.
     *
     * @param data Data to be input
     */
    public static void loadNewData(DataWrapper data) throws SQLException {
        mOpenHelper.resetDB(mDatabase);

        Log.d(RHCareerFairLayout.RH_CFL, "Loading new data");

        bulkInsertWithOnConflict(TABLE_CATEGORY_NAME, data.getCategoryMap().getContentValues(), SQLiteDatabase.CONFLICT_IGNORE);
        bulkInsertWithOnConflict(TABLE_SELECTED_CATEGORIES_NAME, data.getCategoryMap().getSelectionContentValues(false), SQLiteDatabase.CONFLICT_IGNORE);
        bulkInsertWithOnConflict(TABLE_COMPANY_NAME, data.getCompanyMap().getContentValues(), SQLiteDatabase.CONFLICT_FAIL);
        bulkInsertWithOnConflict(TABLE_SELECTED_COMPANIES_NAME, data.getCompanyMap().getSelectionContentValues(true), SQLiteDatabase.CONFLICT_FAIL);
        bulkInsertWithOnConflict(TABLE_COMPANYCATEGORY_NAME, data.getCompanyCategoryMap().getContentValues(), SQLiteDatabase.CONFLICT_FAIL);
        bulkInsertWithOnConflict(TABLE_TABLEMAPPING_NAME, data.getTableMappingList().getContentValues(), SQLiteDatabase.CONFLICT_FAIL);

        ContentValues termRow = data.getTerm().toContentValues();

        mDatabase.insert(TABLE_TERM_NAME, null, termRow);

        setFilteredCompaniesSelected(true);

        Log.d(RHCareerFairLayout.RH_CFL, "Finished loading new data");
    }

    /**
     * Sets a category selected/deselected, and resets company selections to match categories.
     *
     * @param categoryId categoryId to change
     * @param selected whether category with id as above is now true/false.
     */
    public static void setCategorySelected(long categoryId, boolean selected) throws SQLException {

        ContentValues row = new ContentValues();

        row.put(DBManager.KEY_CATEGORY_ID, categoryId);
        row.put(DBManager.KEY_SELECTED, selected);

        mDatabase.insertWithOnConflict(TABLE_SELECTED_CATEGORIES_NAME, null, row, SQLiteDatabase.CONFLICT_REPLACE);


        setFilteredCompaniesSelected(true);

        // Update with only new companies.

    }

    /**
     * Selects all categories selected/deselected
     *
     * @param selected boolean value of bulk-selection
     */
    public static void setAllCategoriesSelected(boolean selected) throws SQLException {
        mDatabase.execSQL("UPDATE " + TABLE_SELECTED_CATEGORIES_NAME +
                        " SET " + KEY_SELECTED + " = " + (selected ? 1 : 0)
        );

        // Update selected company list


        mDatabase.execSQL("UPDATE " + TABLE_SELECTED_COMPANIES_NAME +
                        " SET " + KEY_SELECTED + " = 0"
        );

        CompanyMap companies = new CompanyMap(getFilteredCompaniesCursor());
        bulkInsertWithOnConflict(TABLE_SELECTED_COMPANIES_NAME, companies.getSelectionContentValues(true), SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Sets company selected/deselected
     *
     * @param companyId company to change selection flag
     * @param selected selection flag.
     */
    public static void setCompanySelected(long companyId, boolean selected) {

        ContentValues row = new ContentValues();

        row.put(DBManager.KEY_COMPANY_ID, companyId);
        row.put(DBManager.KEY_SELECTED, selected);

        mDatabase.insertWithOnConflict(TABLE_SELECTED_COMPANIES_NAME, null, row, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Sets all companies matching currently selected filters selected/deselected.
     *
     * @param selected boolean flag to be set on all companies.
     */
    public static void setFilteredCompaniesSelected(boolean selected) throws SQLException {

        // Set selected values of all companies to false
        mDatabase.execSQL("UPDATE " + TABLE_SELECTED_COMPANIES_NAME +
                        " SET " + KEY_SELECTED + " = 0"
        );

        CompanyMap companies = new CompanyMap(getFilteredCompaniesCursor());

        bulkInsertWithOnConflict(TABLE_SELECTED_COMPANIES_NAME, companies.getSelectionContentValues(selected), SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Get all categories
     * @return Cursor of data from categories table, with selection flags appended.
     */
    public static Cursor getCategoriesCursor() {

        // Create new querybuilder
        SQLiteQueryBuilder sqlQB = new SQLiteQueryBuilder();

        // Join tables on ID
        sqlQB.setTables(TABLE_CATEGORY_NAME +
                " JOIN " + TABLE_SELECTED_CATEGORIES_NAME + " ON " +
                KEY_ID + " = " + KEY_CATEGORY_ID);

        // Get these columns.
        String[] projection = new String[]{TABLE_CATEGORY_NAME + "." + KEY_PRIMARY_ID + " AS " + KEY_PRIMARY_ID, KEY_ID, KEY_NAME, KEY_TYPE, KEY_SELECTED};

        // Sort first by type, then name.
        String orderBy = KEY_TYPE + " COLLATE NOCASE ASC, " + KEY_NAME + " COLLATE NOCASE ASC";

        //Get cursor
        return sqlQB.query(mDatabase, projection, null, null, null, null, orderBy);
    }

    public static Cursor getFilteredCompaniesCursor() {
        String searchText = "%" +
                mContext.getSharedPreferences(RHCareerFairLayout.RH_CFL, Context.MODE_PRIVATE).
                        getString(RHCareerFairLayout.PREF_KEY_SEARCH_STRING, "").trim() +
                "%";

        // Create new querybuilder
        SQLiteQueryBuilder sqlQB = new SQLiteQueryBuilder();

        // Join tables on ID
        sqlQB.setTables(TABLE_COMPANY_NAME +
                        " JOIN " + VIEW_FILTERED_COMPANIES_NAME + " ON " +
                        TABLE_COMPANY_NAME + "." + KEY_ID + " = " + VIEW_FILTERED_COMPANIES_NAME + "." + KEY_COMPANY_ID +
                        " JOIN " + TABLE_TABLEMAPPING_NAME + " ON " +
                        TABLE_COMPANY_NAME + "." + KEY_ID + " = " + TABLE_TABLEMAPPING_NAME + "." + KEY_COMPANY_ID +
                        " JOIN " + TABLE_SELECTED_COMPANIES_NAME + " ON " +
                        TABLE_COMPANY_NAME + "." + KEY_ID + " = " + TABLE_SELECTED_COMPANIES_NAME + "." + KEY_COMPANY_ID
        );

        // Get these columns.
        String[] projection = new String[]{
                TABLE_COMPANY_NAME + "." + KEY_PRIMARY_ID + " AS " + KEY_PRIMARY_ID,
                TABLE_COMPANY_NAME + "." + KEY_ID + " AS " + KEY_ID,
                KEY_NAME,
                KEY_DESCRIPTION,
                KEY_WEBSITE_LINK,
                KEY_ADDRESS,
                TABLE_TABLEMAPPING_NAME + "." + KEY_ID + " AS " + KEY_TABLE,
                KEY_SELECTED
        };

//        String where = TABLE_COMPANY_NAME + "." + KEY_NAME + " LIKE '%" + searchText + "%'";
        String where = TABLE_COMPANY_NAME + "." + KEY_NAME + " LIKE ?";
        String[] whereArgs = new String[]{searchText};

        // Sort first by type, then name.
        String orderBy = KEY_NAME + " COLLATE NOCASE ASC";

        //Get cursor
        return sqlQB.query(mDatabase, projection, where, whereArgs, null, null, orderBy);
    }

    public static Cursor getCompaniesCursor() {

        // Create new querybuilder
        SQLiteQueryBuilder sqlQB = new SQLiteQueryBuilder();

        // Join tables on ID
        sqlQB.setTables(TABLE_COMPANY_NAME +
                " JOIN " + TABLE_TABLEMAPPING_NAME + " ON " +
                TABLE_COMPANY_NAME + "." + KEY_ID + " = " + TABLE_TABLEMAPPING_NAME + "." + KEY_COMPANY_ID +
                " JOIN " + TABLE_SELECTED_COMPANIES_NAME + " ON " +
                TABLE_COMPANY_NAME + "." + KEY_ID + " = " + TABLE_SELECTED_COMPANIES_NAME + "." + KEY_COMPANY_ID);

        // Get these columns.
        String[] projection = new String[]{
                TABLE_COMPANY_NAME + "." + KEY_PRIMARY_ID + " AS " + KEY_PRIMARY_ID,
                TABLE_COMPANY_NAME + "." + KEY_ID + " AS " + KEY_ID,
                KEY_NAME,
                KEY_DESCRIPTION,
                KEY_WEBSITE_LINK,
                KEY_ADDRESS,
                TABLE_TABLEMAPPING_NAME + "." + KEY_ID + " AS " + KEY_TABLE,
                KEY_SELECTED
        };

        // Sort first by type, then name.
        String orderBy = KEY_NAME + " COLLATE NOCASE ASC";

        //Get cursor
        return sqlQB.query(mDatabase, projection, null, null, null, null, orderBy);
    }

    public static Company getCompany(long companyId) {
        Cursor cursor = null;
        try {
            // Get these columns.
            String[] projection = new String[]{
                    KEY_ID,
                    KEY_NAME,
                    KEY_DESCRIPTION,
                    KEY_WEBSITE_LINK,
                    KEY_ADDRESS
            };

            // Fitting these conditions
            String where = TABLE_COMPANY_NAME + "." + KEY_ID + " = " + companyId;

            //Get cursor
            cursor = mDatabase.query(TABLE_COMPANY_NAME, projection, where, null, null, null, null);

            if (cursor.moveToFirst()) {
                return new Company(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static HashMap<String, ArrayList<Category>> getCategoriesForCompany(long companyId) {

        Cursor cursor = null;

        try {
            // Create new querybuilder
            SQLiteQueryBuilder sqlQB = new SQLiteQueryBuilder();

            // Join tables on ID
            sqlQB.setTables(TABLE_COMPANYCATEGORY_NAME +
                    " JOIN " + TABLE_CATEGORY_NAME + " ON " +
                    TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " = " + TABLE_CATEGORY_NAME + "." + KEY_ID);

            // Get these columns.
            String[] projection = new String[]{
                    KEY_ID,
                    KEY_NAME,
                    KEY_TYPE
            };

            // Fitting these conditions
            String where = TABLE_COMPANYCATEGORY_NAME + "." + KEY_COMPANY_ID + " = " + companyId;

            // Sort first by type, then name.
            String orderBy = KEY_NAME + " COLLATE NOCASE ASC";

            cursor = sqlQB.query(mDatabase, projection, where, null, null, null, orderBy);

            if (cursor.getCount() > 0) {
                HashMap<String, ArrayList<Category>> categories = new HashMap<String, ArrayList<Category>>();
                while (cursor.moveToNext()) {
                    Category category = new Category(cursor);
                    if (categories.get(category.getType()) == null) {
                        categories.put(category.getType(), new ArrayList<Category>());
                    }
                    categories.get(category.getType()).add(category);
                }

                //Get cursor
                return categories;
            }

            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static TableMapping getTableMappingForCompany(long companyId) {
        Cursor cursor = null;
        try {
            // Get these columns.
            String[] projection = new String[]{
                    KEY_ID,
                    KEY_COMPANY_ID,
                    KEY_SIZE
            };

            // Fitting these conditions
            String where = KEY_COMPANY_ID + " = " + companyId;

            //Get cursor
            cursor = mDatabase.query(TABLE_TABLEMAPPING_NAME, projection, where, null, null, null, null);

            if (cursor.moveToFirst()) {
                return new TableMapping(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static TableMapping getTableMapping(long tableId) {
        Cursor cursor = null;
        try {
            // Get these columns.
            String[] projection = new String[]{
                    KEY_ID,
                    KEY_COMPANY_ID,
                    KEY_SIZE
            };

            // Fitting these conditions
            String where = KEY_ID + " = " + tableId;

            //Get cursor
            cursor = mDatabase.query(TABLE_TABLEMAPPING_NAME, projection, where, null, null, null, null);

            if (cursor.moveToFirst()) {
                return new TableMapping(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Term getTerm() {
        String[] projection = new String[]{KEY_YEAR, KEY_QUARTER, KEY_LAYOUT_SECTION1,
                KEY_LAYOUT_SECTION2, KEY_LAYOUT_SECTION2_PATHWIDTH, KEY_LAYOUT_SECTION2_ROWS,
                KEY_LAYOUT_SECTION3, KEY_LAST_UPDATE_TIME};
        Cursor cursor = mDatabase.query(TABLE_TERM_NAME, projection, null, null, null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                return new Term(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static TableMappingArray getTableMappings() {
        String[] projection = new String[]{KEY_ID, KEY_COMPANY_ID, KEY_SIZE};
        Cursor cursor = mDatabase.query(TABLE_TABLEMAPPING_NAME, projection, null, null, null, null, KEY_ID + " ASC");

        try {
            if (cursor != null && cursor.moveToFirst()) {
                return new TableMappingArray(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Company getCompanyForTableMapping(long tableId) {

        Cursor cursor = null;

        try {
            // Create new querybuilder
            SQLiteQueryBuilder sqlQB = new SQLiteQueryBuilder();

            // Join tables on ID
            sqlQB.setTables(TABLE_COMPANY_NAME +
                    " JOIN " + TABLE_TABLEMAPPING_NAME + " ON " +
                    TABLE_COMPANY_NAME + "." + KEY_ID + " = " + TABLE_TABLEMAPPING_NAME + "." + KEY_COMPANY_ID);

            // Get these columns.
            String[] projection = new String[]{
                    TABLE_COMPANY_NAME + "." + KEY_ID + " AS " + KEY_ID,
                    KEY_NAME,
                    KEY_DESCRIPTION,
                    KEY_WEBSITE_LINK,
                    KEY_ADDRESS
            };

            // Fitting these conditions
            String where = TABLE_TABLEMAPPING_NAME + "." + KEY_ID + " = " + tableId;

            cursor = sqlQB.query(mDatabase, projection, where, null, null, null, null);

            if (cursor.moveToFirst()) {
                return new Company(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static TableMap getTables() {

        // Create new querybuilder
        SQLiteQueryBuilder sqlQB = new SQLiteQueryBuilder();

        // Join tables on ID
        sqlQB.setTables(TABLE_TABLEMAPPING_NAME +
                " LEFT OUTER JOIN " + TABLE_SELECTED_COMPANIES_NAME + " ON " +
                TABLE_TABLEMAPPING_NAME + "." + KEY_COMPANY_ID + " = " + TABLE_SELECTED_COMPANIES_NAME + "." + KEY_COMPANY_ID);

        // Get these columns.
        String[] projection = new String[]{
                KEY_ID,
                KEY_SIZE,
                KEY_SELECTED
        };

        // Sort first by type, then name.
        String orderBy = KEY_ID + " ASC";

        //Get cursor
        Cursor cursor = sqlQB.query(mDatabase, projection, null, null, null, null, orderBy);

        return new TableMap(cursor);
    }

    private static int bulkInsertWithOnConflict(String table, ContentValues[] values, int conflictAlgorithm) throws SQLException {
        int numInserted = 0;

        mDatabase.beginTransaction();
        try {
            for (ContentValues cv : values) {

                long newID = mDatabase.insertWithOnConflict(
                        table,
                        null,
                        cv,
                        conflictAlgorithm);
                if (newID <= 0) {
                    Log.d(RHCareerFairLayout.RH_CFL, "Conflict on content value " + cv.toString() +
                            "; Using conflict algorithim " + conflictAlgorithm);
                }
            }
            numInserted = values.length;
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
        return numInserted;
    }

    public static void close() {
        mOpenHelper.close();
        mOpenHelper = null;
        mDatabase = null;
    }

    private static class DBHelper extends SQLiteOpenHelper {

        public static final String CREATE_TABLE_CATEGORY =
                "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORY_NAME + " (" +
                        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_ID + " INTEGER NOT NULL, " +
                        KEY_NAME + " VARCHAR(100) NOT NULL, " +
                        KEY_TYPE + " VARCHAR(50) NOT NULL, " +
                        "UNIQUE (" + KEY_ID + "), " +
                        "UNIQUE(" + KEY_NAME + ", " + KEY_TYPE + ") " +
                        ");";

        public static final String CREATE_TABLE_COMPANY =
                "CREATE TABLE IF NOT EXISTS " + TABLE_COMPANY_NAME + " (" +
                        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_ID + " INTEGER NOT NULL, " +
                        KEY_NAME + " VARCHAR(100) NOT NULL, " +
                        KEY_DESCRIPTION + " TEXT NOT NULL, " +
                        KEY_WEBSITE_LINK + " VARCHAR(100) NOT NULL, " +
                        KEY_ADDRESS + " VARCHAR(250) NOT NULL, " +
                        "UNIQUE (" + KEY_ID + ") " +
                        ");";

        public static final String CREATE_TABLE_COMPANYCATEGORY =
                "CREATE TABLE IF NOT EXISTS " + TABLE_COMPANYCATEGORY_NAME + " (" +
                        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_COMPANY_ID + " INTEGER NOT NULL, " +
                        KEY_CATEGORY_ID + " INTEGER NOT NULL, " +
                        "UNIQUE (" + KEY_COMPANY_ID + ", " + KEY_CATEGORY_ID + "), " +
                        "FOREIGN KEY (" + KEY_COMPANY_ID + ") REFERENCES " + TABLE_COMPANY_NAME + "(" + KEY_ID + ") ON UPDATE CASCADE ON DELETE CASCADE, " +
                        "FOREIGN KEY (" + KEY_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY_NAME + "(" + KEY_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
                        ");";

        public static final String CREATE_TABLE_TABLEMAPPING =
                "CREATE TABLE IF NOT EXISTS " + TABLE_TABLEMAPPING_NAME + " (" +
                        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_ID + " INTEGER NOT NULL, " +
                        KEY_COMPANY_ID + " INTEGER, " +
                        KEY_SIZE + " INTEGER NOT NULL, " +
                        "UNIQUE (" + KEY_ID + "), " +
                        "FOREIGN KEY (" + KEY_COMPANY_ID + ") REFERENCES " + TABLE_COMPANY_NAME + "(" + KEY_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
                        ");";


        public static final String CREATE_TABLE_TERM =
                "CREATE TABLE IF NOT EXISTS " + TABLE_TERM_NAME + " (" +
                        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_YEAR + " INTEGER NOT NULL, " +
                        KEY_QUARTER + " VARCHAR(10) NOT NULL, " +
                        KEY_LAYOUT_SECTION1 + " INTEGER NOT NULL, " +
                        KEY_LAYOUT_SECTION2 + " INTEGER NOT NULL, " +
                        KEY_LAYOUT_SECTION2_PATHWIDTH + " INTEGER NOT NULL, " +
                        KEY_LAYOUT_SECTION2_ROWS + " INTEGER NOT NULL, " +
                        KEY_LAYOUT_SECTION3 + " INTEGER NOT NULL, " +
                        KEY_LAST_UPDATE_TIME + " INTEGER NOT NULL, " +
                        "UNIQUE (" + KEY_YEAR + ", " + KEY_QUARTER + ") " +
                        ");";

        public static final String CREATE_TABLE_SELECTED_CATEGORIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_SELECTED_CATEGORIES_NAME + " (" +
                        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_CATEGORY_ID + " INTEGER NOT NULL, " +
                        KEY_SELECTED + " BOOLEAN NOT NULL DEFAULT 0, " +
                        "UNIQUE (" + KEY_CATEGORY_ID + "), " +
                        "FOREIGN KEY (" + KEY_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY_NAME + "(" + KEY_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
                        ");";

        public static final String CREATE_TABLE_SELECTED_COMPANIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_SELECTED_COMPANIES_NAME + " (" +
                        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_COMPANY_ID + " INTEGER NOT NULL, " +
                        KEY_SELECTED + " BOOLEAN NOT NULL DEFAULT 0, " +
                        "UNIQUE (" + KEY_COMPANY_ID + "), " +
                        "FOREIGN KEY (" + KEY_COMPANY_ID + ") REFERENCES " + TABLE_COMPANY_NAME + "(" + KEY_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
                        ");";

        public static String CREATE_VIEW_FILTERED_COMPANIES;

        static {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE VIEW IF NOT EXISTS " + VIEW_FILTERED_COMPANIES_NAME + " AS");
            sb.append(" SELECT " + VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME + "." + KEY_COMPANY_ID + " AS " + KEY_COMPANY_ID);
            sb.append(" FROM " + VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME + " JOIN ");
            sb.append(VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE_NAME + " ON ");
            sb.append(VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME + "." + KEY_COMPANY_ID + " = ");
            sb.append(VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE_NAME + "." + KEY_COMPANY_ID);
            sb.append(" JOIN ");
            sb.append(VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION_NAME + " ON ");
            sb.append(VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME + "." + KEY_COMPANY_ID + " = ");
            sb.append(VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION_NAME + "." + KEY_COMPANY_ID + ";");
            CREATE_VIEW_FILTERED_COMPANIES = sb.toString();
        }

        public static String CREATE_VIEW_FILTERED_COMPANIES_BY_MAJOR;

        static {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE VIEW IF NOT EXISTS " + VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME + " AS");
            sb.append(" SELECT DISTINCT " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_COMPANY_ID);
            sb.append(" FROM " + TABLE_COMPANYCATEGORY_NAME + " JOIN " + TABLE_SELECTED_CATEGORIES_NAME);
            sb.append(" ON " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " = ");
            sb.append(TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID);
            sb.append(" JOIN " + TABLE_CATEGORY_NAME);
            sb.append(" ON " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " = ");
            sb.append(TABLE_CATEGORY_NAME + "." + KEY_ID);
            sb.append(" WHERE ((NOT EXISTS(SELECT 1");
            sb.append(" FROM " + TABLE_SELECTED_CATEGORIES_NAME + " JOIN " + TABLE_CATEGORY_NAME);
            sb.append(" ON " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID + " = ");
            sb.append(TABLE_CATEGORY_NAME + "." + KEY_ID);
            sb.append(" WHERE type = 'Major' AND " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_SELECTED + " = 1))");
            sb.append(" OR (" + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " IN(");
            sb.append(" SELECT " + TABLE_CATEGORY_NAME + "." + KEY_ID);
            sb.append(" FROM " + TABLE_SELECTED_CATEGORIES_NAME + " JOIN " + TABLE_CATEGORY_NAME);
            sb.append(" ON " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID + " = ");
            sb.append(TABLE_CATEGORY_NAME + "." + KEY_ID);
            sb.append(" WHERE type = 'Major' AND " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_SELECTED + " = 1)));");
            CREATE_VIEW_FILTERED_COMPANIES_BY_MAJOR = sb.toString();
        }

        public static String CREATE_VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION;

        static {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE VIEW IF NOT EXISTS " + VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION_NAME + " AS");
            sb.append(" SELECT DISTINCT " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_COMPANY_ID);
            sb.append(" FROM " + TABLE_COMPANYCATEGORY_NAME + " JOIN " + TABLE_SELECTED_CATEGORIES_NAME);
            sb.append(" ON " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " = ");
            sb.append(TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID);
            sb.append(" JOIN " + TABLE_CATEGORY_NAME);
            sb.append(" ON " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " = ");
            sb.append(TABLE_CATEGORY_NAME + "." + KEY_ID);
            sb.append(" WHERE ((NOT EXISTS(SELECT 1");
            sb.append(" FROM " + TABLE_SELECTED_CATEGORIES_NAME + " JOIN " + TABLE_CATEGORY_NAME);
            sb.append(" ON " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID + " = ");
            sb.append(TABLE_CATEGORY_NAME + "." + KEY_ID);
            sb.append(" WHERE type = 'Work Authorization' AND " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_SELECTED + " = 1))");
            sb.append(" OR (" + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " IN(");
            sb.append(" SELECT " + TABLE_CATEGORY_NAME + "." + KEY_ID);
            sb.append(" FROM " + TABLE_SELECTED_CATEGORIES_NAME + " JOIN " + TABLE_CATEGORY_NAME);
            sb.append(" ON " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID + " = ");
            sb.append(TABLE_CATEGORY_NAME + "." + KEY_ID);
            sb.append(" WHERE type = 'Work Authorization' AND " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_SELECTED + " = 1)));");
            CREATE_VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION = sb.toString();
        }

        public static String CREATE_VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE;

        static {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE VIEW IF NOT EXISTS " + VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE_NAME + " AS");
            sb.append(" SELECT DISTINCT " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_COMPANY_ID);
            sb.append(" FROM " + TABLE_COMPANYCATEGORY_NAME + " JOIN " + TABLE_SELECTED_CATEGORIES_NAME);
            sb.append(" ON " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " = ");
            sb.append(TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID);
            sb.append(" JOIN " + TABLE_CATEGORY_NAME);
            sb.append(" ON " + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " = ");
            sb.append(TABLE_CATEGORY_NAME + "." + KEY_ID);
            sb.append(" WHERE ((NOT EXISTS(SELECT 1");
            sb.append(" FROM " + TABLE_SELECTED_CATEGORIES_NAME + " JOIN " + TABLE_CATEGORY_NAME);
            sb.append(" ON " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID + " = ");
            sb.append(TABLE_CATEGORY_NAME + "." + KEY_ID);
            sb.append(" WHERE type = 'Position Type' AND " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_SELECTED + " = 1))");
            sb.append(" OR (" + TABLE_COMPANYCATEGORY_NAME + "." + KEY_CATEGORY_ID + " IN(");
            sb.append(" SELECT " + TABLE_CATEGORY_NAME + "." + KEY_ID);
            sb.append(" FROM " + TABLE_SELECTED_CATEGORIES_NAME + " JOIN " + TABLE_CATEGORY_NAME);
            sb.append(" ON " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_CATEGORY_ID + " = ");
            sb.append(TABLE_CATEGORY_NAME + "." + KEY_ID);
            sb.append(" WHERE type = 'Position Type' AND " + TABLE_SELECTED_CATEGORIES_NAME + "." + KEY_SELECTED + " = 1)));");
            CREATE_VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE = sb.toString();
        }

        public static final String DROP_TABLE_CATEGORY = "DROP TABLE IF EXISTS " + TABLE_CATEGORY_NAME;
        public static final String DROP_TABLE_COMPANY = "DROP TABLE IF EXISTS " + TABLE_COMPANY_NAME;
        public static final String DROP_TABLE_COMPANYCATEGORY = "DROP TABLE IF EXISTS " + TABLE_COMPANYCATEGORY_NAME;
        public static final String DROP_TABLE_TABLEMAPPING = "DROP TABLE IF EXISTS " + TABLE_TABLEMAPPING_NAME;
        public static final String DROP_TABLE_TERM = "DROP TABLE IF EXISTS " + TABLE_TERM_NAME;
        public static final String DROP_TABLE_SELECTED_CATEGORIES = "DROP TABLE IF EXISTS " + TABLE_SELECTED_CATEGORIES_NAME;
        public static final String DROP_TABLE_SELECTED_COMPANIES = "DROP TABLE IF EXISTS " + TABLE_SELECTED_COMPANIES_NAME;
        public static final String DROP_VIEW_FILTERED_COMPANIES = "DROP VIEW IF EXISTS " + VIEW_FILTERED_COMPANIES_NAME;
        public static final String DROP_VIEW_FILTERED_COMPANIES_BY_MAJOR = "DROP VIEW IF EXISTS " + VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME;
        public static final String DROP_VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE = "DROP VIEW IF EXISTS " + VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE_NAME;
        public static final String DROP_VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION = "DROP VIEW IF EXISTS " + VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION_NAME;

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void resetDB(SQLiteDatabase db) {

            dropTables(db);
            createTables(db);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTables(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            dropViews(db);
            dropCategories(db);
            resetDB(db);
        }

        public void createTables(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_CATEGORY);
            db.execSQL(CREATE_TABLE_SELECTED_CATEGORIES);
            db.execSQL(CREATE_TABLE_COMPANY);
            db.execSQL(CREATE_TABLE_SELECTED_COMPANIES);
            db.execSQL(CREATE_TABLE_COMPANYCATEGORY);
            db.execSQL(CREATE_TABLE_TABLEMAPPING);
            db.execSQL(CREATE_TABLE_TERM);
            db.execSQL(CREATE_VIEW_FILTERED_COMPANIES_BY_MAJOR);
            db.execSQL(CREATE_VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE);
            db.execSQL(CREATE_VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION);
            db.execSQL(CREATE_VIEW_FILTERED_COMPANIES);
        }

        public void dropTables(SQLiteDatabase db) {
            db.execSQL(DROP_TABLE_COMPANY);
            db.execSQL(DROP_TABLE_SELECTED_COMPANIES);
            db.execSQL(DROP_TABLE_COMPANYCATEGORY);
            db.execSQL(DROP_TABLE_TABLEMAPPING);
            db.execSQL(DROP_TABLE_TERM);
        }

        public void dropCategories(SQLiteDatabase db) {
            db.execSQL(DROP_TABLE_CATEGORY);
            db.execSQL(DROP_TABLE_SELECTED_CATEGORIES);
        }

        public void dropViews(SQLiteDatabase db) {
            db.execSQL(DROP_VIEW_FILTERED_COMPANIES);
            db.execSQL(DROP_VIEW_FILTERED_COMPANIES_BY_MAJOR);
            db.execSQL(DROP_VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE);
            db.execSQL(DROP_VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION);
        }
    }
}
