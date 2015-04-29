/**
 * Data Servlet
 * 
 * Serves data requests for public site; no authentication required.
 * 
 * @author Benedict Wong
 */
package servlets.data;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import adt.Category;
import adt.Company;
import adt.ItemVars;
import adt.Layout;
import adt.Response;
import adt.Response.FailResponse;

//Use Servlet 3.0 annotations
@WebServlet("/api/data")
public class DataServlet extends HttpServlet {
    
    /**
     * 
     */
    private static final long                serialVersionUID = -5982008108929904358L;
    
    /**
     * Set variables for data - will eventually be moved to database
     */
    public static Layout                 layoutVars;
    public static ItemVars                   systemVars;
    public static HashMap<Integer, Category> categoryMap      = new HashMap<Integer, Category>();
    public static HashMap<Integer, Company>  companyMap       = new HashMap<Integer, Company>();
    
    public DataServlet() throws IOException {
    
        super();
        
        // loadDataFromFile();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
        // get supplied method, use "null" if none given.
        String method = request.getParameter("method") != null ? request.getParameter("method") : "null";
        
        // create response object
        Response responseObject;
        
        // select method based on the parameters sent.
        switch (method) {
            case "getData":
                responseObject = DataRequestHandler.handleGetDataRequest(request);
                break;
            // If invalid method header, return with an error;
            default:
                responseObject = new FailResponse("Invalid GET method supplied to data servlet: " + method);
                break;
        }
        
        // Set return content type and send data;
        response.setContentType("text/plain");
        response.getWriter().print(responseObject);
    }
    
    // public static void loadDataFromFile() throws IOException {
    //
    // loadBasicData();
    //
    // loadCategories();
    //
    // loadCompanyData();
    // }
    //
    // private static void loadBasicData() throws IOException {
    //
    // // import basicData from file
    // DataTable basicData = new DataTable();
    // basicData.importFromResourceFile("BasicData.txt", "\t", true, "\"");
    //
    // // set Year and Quarter
    // DataVars.setYear(basicData.getItem("Year", "Value", Integer.class));
    // DataVars.setQuarter(basicData.getItem("Quarter", "Value", String.class));
    //
    // // set layout data;
    // layoutVars = new LayoutVars();
    // layoutVars.setSection1(basicData.getItem("Layout_Section1", "Value", Integer.class));
    // layoutVars.setSection2(basicData.getItem("Layout_Section2", "Value", Integer.class));
    // layoutVars.setSection2Rows(basicData.getItem("Layout_Section2_Rows", "Value", Integer.class));
    // layoutVars.setSection2PathWidth(basicData.getItem("Layout_Section2_PathWidth", "Value", Integer.class));
    // layoutVars.setSection3(basicData.getItem("Layout_Section3", "Value", Integer.class));
    //
    // // import table mappings
    // DataTable tableMappings = new DataTable();
    // tableMappings.importFromResourceFile("TableMappings.txt", "\t", true, "\"");
    //
    // // generate two-way table mappings.
    // for (int i = 0; i < tableMappings.getRows(); i++) {
    // TableMapping table =
    // new TableMapping(tableMappings.getItem(i, 0, Integer.class), tableMappings.getItem(i, 1, Integer.class), tableMappings.getItem(i,
    // 2, Integer.class));
    // layoutVars.getLocationTableMapping().put(table.location, table);
    // layoutVars.getTableLocationMapping().put(table.tableNumber, table);
    // }
    // }
    //
    // private static void loadCategories() throws IOException {
    //
    // DataTable categories = new DataTable();
    // categories.importFromResourceFile("Categories.txt", "\t", true, "\"");
    //
    // for (int i = 0; i < categories.getRows(); i++) {
    // String title = categories.getItem(i, 0, String.class);
    // String type = categories.getItem(i, 1, String.class);
    //
    // Category newCategory = new Category(title, type);
    // categoryMap.put(newCategory.getID(), newCategory);
    //
    // DataVars.addToIDLookupTable(title, type, newCategory.getID());
    // }
    // }
    //
    // private static void loadCompanyData() throws IOException {
    //
    // DataTable companyData = new DataTable();
    // companyData.importFromResourceFile("CompanyData.txt", "\t", true, "\"");
    //
    // for (int i = 0; i < companyData.getRows(); i++) {
    // String title = companyData.getItem(i, 0, String.class).replace("\"", "");
    // String[] majors = companyData.getItem(i, 1, String.class) == null ? new String[] {} : companyData.getItem(i, 1, String.class).split(",");
    // String[] workAuths =
    // companyData.getItem(i, 2, String.class) == null ? new String[] {} : companyData.getItem(i, 2, String.class).split(",");
    // String[] posTypes =
    // companyData.getItem(i, 3, String.class) == null ? new String[] {} : companyData.getItem(i, 3, String.class).split(",");
    //
    // ArrayList<Integer> majorsList = new ArrayList<Integer>();
    // ArrayList<Integer> workAuthList = new ArrayList<Integer>();
    // ArrayList<Integer> posTypeList = new ArrayList<Integer>();
    //
    // for (String major : majors) {
    // Integer id = DataVars.getFromIDLookupTable("Majors", major);
    // if (id != null) {
    // majorsList.add(DataVars.getFromIDLookupTable("Majors", major));
    // }
    // }
    //
    // for (String workAuth : workAuths) {
    // Integer id = DataVars.getFromIDLookupTable("Work Authorizations", workAuth);
    // if (id != null) {
    // workAuthList.add(DataVars.getFromIDLookupTable("Work Authorizations", workAuth));
    // }
    // }
    //
    // for (String posType : posTypes) {
    // Integer id = DataVars.getFromIDLookupTable("Position Types", posType);
    // if (id != null) {
    // posTypeList.add(DataVars.getFromIDLookupTable("Position Types", posType));
    // }
    // }
    //
    // if (majorsList.isEmpty()) {
    // majorsList.addAll(DataVars.getAllOfType("Majors"));
    // }
    // if (workAuthList.isEmpty()) {
    // workAuthList.addAll(DataVars.getAllOfType("Work Authorizations"));
    // }
    // if (posTypeList.isEmpty()) {
    // posTypeList.addAll(DataVars.getAllOfType("Position Types"));
    // }
    //
    // HashMap<String, List<Integer>> categories = new HashMap<String, List<Integer>>();
    // categories.put("Majors", majorsList);
    // categories.put("Work Authorizations", workAuthList);
    // categories.put("Position Types", posTypeList);
    // Integer tableNumber = companyData.getItem(i, 4, Integer.class);
    //
    // Company newCompany = new Company(title, categories, null, tableNumber);
    //
    // companyMap.put(newCompany.getID(), newCompany);
    // }
    // }
}
