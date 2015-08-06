package servlets;

public class Test {
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
    private static final String VIEW_FILTERED_COMPANIES_BY_WORK_AUTHORIZATION = "FilteredCompaniesByWorkAuthorization";
    private static final String VIEW_FILTERED_COMPANIES_BY_POSITION_TYPE = "FilteredCompaniesByPositionType";

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
    public static String CREATE_VIEW_FILTERED_COMPANIES_BY_MAJOR;

    static {
	StringBuilder sb = new StringBuilder();
	sb.append("CREATE VIEW " + VIEW_FILTERED_COMPANIES_BY_MAJOR_NAME + " AS");
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

    public static void main(String[] args) {

	System.out.println(CREATE_VIEW_FILTERED_COMPANIES_BY_MAJOR);
    }
}