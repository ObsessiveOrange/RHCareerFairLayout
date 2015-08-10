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
    //
    // DB Schema version constant
    private static final int DATABASE_VERSION = 2;
    //
    // Filename constant
    private static final String DATABASE_NAME = "RHCareerFairLayout.db";
    //
    // Table name constants
    private static final String TABLE_CATEGORY_NAME = "Category";
    private static final String TABLE_COMPANY_NAME = "Company";
    private static final String TABLE_COMPANYCATEGORY_NAME = "Company_Category";
    private static final String TABLE_TABLEMAPPING_NAME = "TableMapping";
    private static final String TABLE_TERM_NAME = "Term";
    private static final String TABLE_SELECTED_COMPANIES_NAME = "SelectedCompanies";
    private static final String TABLE_SELECTED_CATEGORIES_NAME = "SelectedCategories";
    //
    // View name constants
    private static final String VIEW_FILTERED_COMPANIES_NAME = "FilteredCompanies";
    private static final String VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME = "FilteredCompaniesByMajor";
    private static final String VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION_NAME = "FilteredCompaniesByWorkAuthorization";
    private static final String VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE_NAME = "FilteredCompaniesByPositionType";

    //
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
    //
    // DB Helpers
    private static DBHelper mOpenHelper = null;
    private static SQLiteDatabase mDatabase = null;

    public static void setupDBAdapterIfNeeded(Context context) {
        // Create a SQLiteOpenHelper
        if (mOpenHelper == null) {
            mOpenHelper = new DBHelper(context);
        }
        // Open the database
        if (mDatabase == null) {
            mDatabase = mOpenHelper.getWritableDatabase();
        }
    }

    public static Long getLastUpdateTime(String year, String quarter) {

        String[] projection = new String[]{KEY_LAST_UPDATE_TIME};
        String whereClause = KEY_YEAR + " = '" + year + "' AND " + KEY_QUARTER + " = '" + quarter + "'";

        Cursor c = mDatabase.query(TABLE_TERM_NAME, projection, whereClause, null, null, null, null);

        if (c.getCount() == 0) {
            return null;
        }

        c.moveToFirst();
        return c.getLong(c.getColumnIndexOrThrow(KEY_LAST_UPDATE_TIME));
    }

    public static void loadNewData(DataWrapper data) throws SQLException {
        mOpenHelper.resetDB(mDatabase);

        Log.d(RHCareerFairLayout.RH_CFL, "Loading new data");

        bulkInsert(TABLE_CATEGORY_NAME, data.getCategoryMap().getContentValues());
        bulkInsert(TABLE_SELECTED_CATEGORIES_NAME, data.getCategoryMap().getSelectionContentValues(false));
        bulkInsert(TABLE_COMPANY_NAME, data.getCompanyMap().getContentValues());
        bulkInsert(TABLE_SELECTED_COMPANIES_NAME, data.getCompanyMap().getSelectionContentValues(true));
        bulkInsert(TABLE_COMPANYCATEGORY_NAME, data.getCompanyCategoryMap().getContentValues());
        bulkInsert(TABLE_TABLEMAPPING_NAME, data.getTableMappingList().getContentValues());

        ContentValues termRow = data.getTerm().toContentValues();

        mDatabase.insert(TABLE_TERM_NAME, null, termRow);

        Log.d(RHCareerFairLayout.RH_CFL, "Finished loading new data");
    }

    public static void setCategorySelected(long categoryId, boolean selected) throws SQLException {

        ContentValues row = new ContentValues();

        row.put(DBManager.KEY_CATEGORY_ID, categoryId);
        row.put(DBManager.KEY_SELECTED, selected);

        mDatabase.insertWithOnConflict(TABLE_SELECTED_CATEGORIES_NAME, null, row, SQLiteDatabase.CONFLICT_REPLACE);

        // Set selected values of all companies to false
        mDatabase.execSQL("UPDATE " + TABLE_SELECTED_COMPANIES_NAME +
                        " SET " + KEY_SELECTED + " = 0"
        );

        CompanyMap companies = new CompanyMap(getFilteredCompaniesCursor());
        bulkInsertOrUpdate(TABLE_SELECTED_COMPANIES_NAME, companies.getSelectionContentValues(true));

        // Update with only new companies.

    }

    public static void setAllCategoriesSelected(boolean selected) throws SQLException {
        mDatabase.execSQL("UPDATE " + TABLE_SELECTED_CATEGORIES_NAME +
                        " SET " + KEY_SELECTED + " = " + (selected ? 1 : 0)
        );

        // Update selected company list


        mDatabase.execSQL("UPDATE " + TABLE_SELECTED_COMPANIES_NAME +
                        " SET " + KEY_SELECTED + " = 0"
        );

        CompanyMap companies = new CompanyMap(getFilteredCompaniesCursor());
        bulkInsertOrUpdate(TABLE_SELECTED_COMPANIES_NAME, companies.getSelectionContentValues(true));
    }

    public static void setCompanySelected(long companyId, boolean selected) {

        ContentValues row = new ContentValues();

        row.put(DBManager.KEY_COMPANY_ID, companyId);
        row.put(DBManager.KEY_SELECTED, selected);

        mDatabase.insertWithOnConflict(TABLE_SELECTED_COMPANIES_NAME, null, row, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static void setAllCompaniesSelected(boolean selected) throws SQLException {
        CompanyMap companies = new CompanyMap(getFilteredCompaniesCursor());

        bulkInsertOrUpdate(TABLE_SELECTED_COMPANIES_NAME, companies.getSelectionContentValues(selected));
    }

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

        // Create new querybuilder
        SQLiteQueryBuilder sqlQB = new SQLiteQueryBuilder();

        // Join tables on ID
        sqlQB.setTables(TABLE_COMPANY_NAME +
                " JOIN " + VIEW_FILTERED_COMPANIES_NAME + " ON " +
                TABLE_COMPANY_NAME + "." + KEY_ID + " = " + VIEW_FILTERED_COMPANIES_NAME + "." + KEY_COMPANY_ID +
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

            if(cursor.getCount() > 0) {
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

    public static TableMapping getTableMappingForCompany(long companyId){
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

    public static Company getCompanyForTableMapping (long tableId){

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
                " LEFT OUTER JOIN " + TABLE_COMPANY_NAME + " ON " +
                TABLE_TABLEMAPPING_NAME + "." + KEY_COMPANY_ID + " = " + TABLE_COMPANY_NAME + "." + KEY_ID +
                " LEFT OUTER JOIN " + TABLE_SELECTED_COMPANIES_NAME + " ON " +
                TABLE_COMPANY_NAME + "." + KEY_ID + " = " + TABLE_SELECTED_COMPANIES_NAME + "." + KEY_COMPANY_ID);

        // Get these columns.
        String[] projection = new String[]{
                TABLE_TABLEMAPPING_NAME + "." + KEY_ID + " AS " + KEY_ID,
                KEY_SIZE,
                KEY_SELECTED
        };

        // Sort first by type, then name.
        String orderBy = KEY_ID + " ASC";

        //Get cursor
        Cursor cursor = sqlQB.query(mDatabase, projection, null, null, null, null, orderBy);

        return new TableMap(cursor);
    }

//    /**
//     * Add a score to the DB
//     *
//     * @param score
//     * @return the id of the item that was added.
//     */
//    public long addScore(Score score) {
//        ContentValues row = getContentValuesFromScore(score);
//        long newId = mDatabase.insert(TABLE_NAME, null, row);
//
//        score.setId(newId);
//        return newId;
//    }
//
//
//
//    public Cursor getScoresCursor() {
//        String[] projection = new String[]{KEY_ID, KEY_NAME, KEY_SCORE};
//        return mDatabase.query(TABLE_NAME, projection, null, null, null, null, KEY_SCORE + " DESC");
//    }
//
//    public Score getScore(long id) {
//        String[] projection = new String[]{KEY_ID, KEY_NAME, KEY_SCORE};
//        String selection = KEY_ID + " = " + id;
//        Cursor c = mDatabase.query(TABLE_NAME, projection, selection, null, null, null, KEY_SCORE + " DESC");
//
//        if (c != null && c.moveToFirst()) {
//            return getScoreFromCursor(c);
//        }
//        return null;
//    }
//
//    public void updateScore(Score score) {
//        ContentValues row = getContentValuesFromScore(score);
//        String whereClause = KEY_ID + " = " + score.getId();
//        mDatabase.update(TABLE_NAME, row, whereClause, null);
//    }
//    private ContentValues getContentValuesFromScore(Score score) {
//        ContentValues row = new ContentValues();
//        row.put(KEY_NAME, score.getTitle());
//        row.put(KEY_SCORE, score.getScore());
//        return row;
//    }
//    private Score getScoreFromCursor(Cursor c) {
//        Score score = new Score();
//        score.setId(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
//        score.setTitle(c.getString(c.getColumnIndexOrThrow(KEY_NAME)));
//        score.setScore(c.getInt(c.getColumnIndexOrThrow(KEY_SCORE)));
//        return score;
//    }
//
//    public boolean removeScore(long id) {
//        String whereClause = KEY_ID + " = " + id;
//        return mDatabase.delete(TABLE_NAME, whereClause, null) > 0;
//    }

    private static int bulkInsert(String table, ContentValues[] values) throws SQLException {
        int numInserted = 0;

        mDatabase.beginTransaction();
        try {
            for (ContentValues cv : values) {
                long newID = mDatabase.insertOrThrow(table, null, cv);
                if (newID <= 0) {
                    throw new SQLException("Failed to insert row into " + table);
                }
            }
            numInserted = values.length;
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
        return numInserted;
    }

    private static int bulkInsertOrUpdate(String table, ContentValues[] values) throws SQLException {
        int numInserted = 0;

        mDatabase.beginTransaction();
        try {
            for (ContentValues cv : values) {

                long newID = mDatabase.insertWithOnConflict(
                        table,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_REPLACE);
                if (newID <= 0) {
                    throw new SQLException("Failed to insert row into " + table);
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

        public static final String CREATE_CATEGORY_STATEMENT =
                "CREATE TABLE " + TABLE_CATEGORY_NAME + " (" +
                        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_ID + " INTEGER NOT NULL, " +
                        KEY_NAME + " VARCHAR(100) NOT NULL, " +
                        KEY_TYPE + " VARCHAR(50) NOT NULL, " +
                        "UNIQUE (" + KEY_ID + "), " +
                        "UNIQUE(" + KEY_NAME + ", " + KEY_TYPE + ") " +
                        ");";

        public static final String CREATE_COMPANY_STATEMENT =
                "CREATE TABLE " + TABLE_COMPANY_NAME + " (" +
                        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_ID + " INTEGER NOT NULL, " +
                        KEY_NAME + " VARCHAR(100) NOT NULL, " +
                        KEY_DESCRIPTION + " TEXT NOT NULL, " +
                        KEY_WEBSITE_LINK + " VARCHAR(100) NOT NULL, " +
                        KEY_ADDRESS + " VARCHAR(250) NOT NULL, " +
                        "UNIQUE (" + KEY_ID + ") " +
                        ");";

        public static final String CREATE_COMPANYCATEGORY_STATEMENT =
                "CREATE TABLE " + TABLE_COMPANYCATEGORY_NAME + " (" +
                        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_COMPANY_ID + " INTEGER NOT NULL, " +
                        KEY_CATEGORY_ID + " INTEGER NOT NULL, " +
                        "UNIQUE (" + KEY_COMPANY_ID + ", " + KEY_CATEGORY_ID + "), " +
                        "FOREIGN KEY (" + KEY_COMPANY_ID + ") REFERENCES " + TABLE_COMPANY_NAME + "(" + KEY_ID + ") ON UPDATE CASCADE ON DELETE CASCADE, " +
                        "FOREIGN KEY (" + KEY_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY_NAME + "(" + KEY_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
                        ");";

        public static final String CREATE_TABLEMAPPING_STATEMENT =
                "CREATE TABLE " + TABLE_TABLEMAPPING_NAME + " (" +
                        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_ID + " INTEGER NOT NULL, " +
                        KEY_COMPANY_ID + " INTEGER, " +
                        KEY_SIZE + " INTEGER NOT NULL, " +
                        "UNIQUE (" + KEY_ID + "), " +
                        "FOREIGN KEY (" + KEY_COMPANY_ID + ") REFERENCES " + TABLE_COMPANY_NAME + "(" + KEY_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
                        ");";


        public static final String CREATE_TERM_STATEMENT =
                "CREATE TABLE " + TABLE_TERM_NAME + " (" +
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

        public static final String CREATE_SELECTED_CATEGORIES_STATEMENT =
                "CREATE TABLE " + TABLE_SELECTED_CATEGORIES_NAME + " (" +
                        KEY_PRIMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_CATEGORY_ID + " INTEGER NOT NULL, " +
                        KEY_SELECTED + " BOOLEAN NOT NULL DEFAULT 0, " +
                        "UNIQUE (" + KEY_CATEGORY_ID + "), " +
                        "FOREIGN KEY (" + KEY_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY_NAME + "(" + KEY_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
                        ");";

        public static final String CREATE_SELECTED_COMPANIES_STATEMENT =
                "CREATE TABLE " + TABLE_SELECTED_COMPANIES_NAME + " (" +
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

        public static final String DROP_CATEGORY_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_CATEGORY_NAME;
        public static final String DROP_COMPANY_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_COMPANY_NAME;
        public static final String DROP_CATEGORYCOMPANY_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_COMPANYCATEGORY_NAME;
        public static final String DROP_TABLEMAPPING_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_TABLEMAPPING_NAME;
        public static final String DROP_TERM_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_TERM_NAME;
        public static final String DROP_SELECTED_CATEGORIES_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_SELECTED_CATEGORIES_NAME;
        public static final String DROP_SELECTED_COMPANIES_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_SELECTED_COMPANIES_NAME;

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
            resetDB(db);
        }

        public void createTables(SQLiteDatabase db) {
            db.execSQL(CREATE_CATEGORY_STATEMENT);
            db.execSQL(CREATE_COMPANY_STATEMENT);
            db.execSQL(CREATE_COMPANYCATEGORY_STATEMENT);
            db.execSQL(CREATE_TABLEMAPPING_STATEMENT);
            db.execSQL(CREATE_TERM_STATEMENT);
            db.execSQL(CREATE_SELECTED_CATEGORIES_STATEMENT);
            db.execSQL(CREATE_SELECTED_COMPANIES_STATEMENT);
            db.execSQL(CREATE_VIEW_FILTERED_COMPANIES_BY_MAJOR);
            db.execSQL(CREATE_VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE);
            db.execSQL(CREATE_VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION);
            db.execSQL(CREATE_VIEW_FILTERED_COMPANIES);
        }

        public void dropTables(SQLiteDatabase db) {
            db.execSQL(DROP_CATEGORY_STATEMENT);
            db.execSQL(DROP_COMPANY_STATEMENT);
            db.execSQL(DROP_CATEGORYCOMPANY_STATEMENT);
            db.execSQL(DROP_TABLEMAPPING_STATEMENT);
            db.execSQL(DROP_TERM_STATEMENT);
            db.execSQL(DROP_SELECTED_CATEGORIES_STATEMENT);
            db.execSQL(DROP_SELECTED_COMPANIES_STATEMENT);
        }
    }
}
