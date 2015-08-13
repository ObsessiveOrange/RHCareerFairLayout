package servlets;

public class Test {

    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_WEBSITE_LINK = "websiteLink";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_COMPANY_ID = "companyId";
    public static final String KEY_CATEGORY_ID = "categoryId";
    public static final String KEY_SIZE = "size";
    //
    // Filename constant
    private static final String DATABASE_NAME = "RHCareerFairLayout.db";
    //
    // Table name constants
    private static final String TABLE_CATEGORY_NAME = "Category";
    private static final String TABLE_COMPANY_NAME = "Company";
    private static final String TABLE_COMPANYCATEGORY_NAME = "Company_Category";
    private static final String TABLE_TABLEMAPPING_NAME = "TableMapping";
    //
    // DB Schema version constant
    private static final int DATABASE_VERSION = 1;

    public static final String CREATE_CATEGORY_STATEMENT = "CREATE TABLE " + TABLE_CATEGORY_NAME + "(" + KEY_ID
	    + " INT NOT NULL, " + KEY_NAME + " VARCHAR(100) NOT NULL, " + KEY_TYPE + " VARCHAR(50) NOT NULL, "
	    + "PRIMARY KEY (" + KEY_ID + "), " + "UNIQUE(" + KEY_NAME + ", " + KEY_TYPE + ")" + ");";

    public static final String CREATE_COMPANY_STATEMENT = "CREATE TABLE " + TABLE_COMPANY_NAME + "(" + KEY_ID
	    + " INT NOT NULL, " + KEY_NAME + " VARCHAR(100) NOT NULL, " + KEY_DESCRIPTION + " TEXT NOT NULL, "
	    + KEY_WEBSITE_LINK + " VARCHAR(100) NOT NULL, " + KEY_ADDRESS + "VARCHAR(250) NOT NULL, " + "PRIMARY KEY ("
	    + KEY_ID + ")" + ");";

    public static final String CREATE_COMPANYCATEGORY_STATEMENT = "CREATE TABLE " + TABLE_COMPANYCATEGORY_NAME + "("
	    + KEY_COMPANY_ID + " INT NOT NULL, " + KEY_CATEGORY_ID + " VARCHAR(100) NOT NULL, " + "PRIMARY KEY ("
	    + KEY_COMPANY_ID + ", " + KEY_CATEGORY_ID + "), " + "FOREIGN KEY (" + KEY_COMPANY_ID + ") REFERENCES "
	    + TABLE_COMPANY_NAME + "(" + KEY_ID + ") ON UPDATE CASCADE ON DELETE CASCADE, " + "FOREIGN KEY ("
	    + KEY_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY_NAME + "(" + KEY_ID
	    + ") ON UPDATE CASCADE ON DELETE CASCADE" + ");";

    public static final String CREATE_TABLEMAPPING_STATEMENT = "CREATE TABLE " + TABLE_TABLEMAPPING_NAME + "(" + KEY_ID
	    + " INT NOT NULL, " + KEY_COMPANY_ID + " INT, " + KEY_SIZE + " INT NOT NULL, " + "PRIMARY KEY (" + KEY_ID
	    + "), " + "FOREIGN KEY (" + KEY_COMPANY_ID + ") REFERENCES " + TABLE_COMPANY_NAME + "(" + KEY_ID
	    + ") ON UPDATE CASCADE ON DELETE CASCADE" + ");";

    public static void main(String[] args) {

	System.out.println(CREATE_CATEGORY_STATEMENT);
	System.out.println(CREATE_COMPANY_STATEMENT);
	System.out.println(CREATE_COMPANYCATEGORY_STATEMENT);
	System.out.println(CREATE_TABLEMAPPING_STATEMENT);
    }
}