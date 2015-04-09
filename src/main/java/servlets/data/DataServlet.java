package servlets.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import misc.ArrayList2D;
import adt.Category;
import adt.Company;
import adt.DataVars;
import adt.ItemVars;
import adt.LayoutVars;
import adt.Response;
import adt.Response.FailResponse;

@WebServlet("/api/data")
public class DataServlet extends HttpServlet {
    
    /**
     * 
     */
    private static final long                serialVersionUID = -5982008108929904358L;
    
    public static LayoutVars                 layoutVars;
    public static ItemVars                   systemVars;
    public static HashMap<Integer, Category> categoryMap      = new HashMap<Integer, Category>();
    public static HashMap<Integer, Company>  companyMap       = new HashMap<Integer, Company>();
    
    /** Getter & Setter Methods **/
    
    public DataServlet() throws IOException {
    
        super();
        
        setupTestData();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
        response.setContentType("text/plain");
        String method = request.getParameter("method") != null ? request.getParameter("method") : "null";
        
        Response responseObject;
        
        switch (method) {
            case "getData":
                responseObject = DataRequestHandler.handleGetDataRequest(request);
                break;
            default:
                responseObject = new FailResponse("Invalid GET method supplied: " + method);
                break;
        }
        response.getWriter().print(responseObject);
    }
    
    public static void setupTestData() throws IOException {
    
        setupBasicData();
        
        setupCategories();
        
        setupCompanyData();
    }
    
    private static void setupBasicData() throws IOException {
    
        ArrayList2D basicData = new ArrayList2D();
        basicData.importFromResourceFile("BasicData.txt", "\t", true);
        DataVars.setYear(basicData.getItem("Year", "Value", Integer.class));
        DataVars.setQuarter(basicData.getItem("Quarter", "Value", String.class));
        
        layoutVars = new LayoutVars();
        layoutVars.setSection1(basicData.getItem("Layout_Section1", "Value", Integer.class));
        layoutVars.setSection2(basicData.getItem("Layout_Section2", "Value", Integer.class));
        layoutVars.setSection2Rows(basicData.getItem("Layout_Section2_Rows", "Value", Integer.class));
        layoutVars.setSection2PathWidth(basicData.getItem("Layout_Section2_PathWidth", "Value", Integer.class));
        layoutVars.setSection3(basicData.getItem("Layout_Section3", "Value", Integer.class));
    }
    
    private static void setupCategories() throws IOException {
    
        ArrayList2D categories = new ArrayList2D();
        categories.importFromResourceFile("Categories.txt", "\t", true);
        
        for (int i = 0; i < categories.getRows(); i++) {
            String title = categories.getItem(i, 0, String.class);
            String type = categories.getItem(i, 1, String.class);
            
            Category newCategory = new Category(title, type);
            categoryMap.put(newCategory.getID(), newCategory);
            
            DataVars.addToIDLookupTable(title, type, newCategory.getID());
        }
    }
    
    private static void setupCompanyData() throws IOException {
    
        ArrayList2D companyData = new ArrayList2D();
        companyData.importFromResourceFile("CompanyData.txt", "\t", true);
        
        for (int i = 0; i < companyData.getRows(); i++) {
            String title = companyData.getItem(i, 0, String.class);
            String[] majors = companyData.getItem(i, 1, String.class) == null ? new String[] {} : companyData.getItem(i, 1, String.class).split(",");
            String[] workAuths =
                    companyData.getItem(i, 2, String.class) == null ? new String[] {} : companyData.getItem(i, 2, String.class).split(",");
            String[] posTypes =
                    companyData.getItem(i, 3, String.class) == null ? new String[] {} : companyData.getItem(i, 3, String.class).split(",");
            
            ArrayList<Integer> majorsList = new ArrayList<Integer>();
            ArrayList<Integer> workAuthList = new ArrayList<Integer>();
            ArrayList<Integer> posTypeList = new ArrayList<Integer>();
            
            for (String major : majors) {
                Integer id = DataVars.getFromIDLookupTable("Majors", major);
                if (id != null) {
                    majorsList.add(DataVars.getFromIDLookupTable("Majors", major));
                }
            }
            
            for (String workAuth : workAuths) {
                Integer id = DataVars.getFromIDLookupTable("Work Authorizations", workAuth);
                if (id != null) {
                    workAuthList.add(DataVars.getFromIDLookupTable("Work Authorizations", workAuth));
                }
            }
            
            for (String posType : posTypes) {
                Integer id = DataVars.getFromIDLookupTable("Position Types", posType);
                if (id != null) {
                    posTypeList.add(DataVars.getFromIDLookupTable("Position Types", posType));
                }
            }
            
            if (majorsList.isEmpty()) {
                majorsList.addAll(DataVars.getAllOfType("Majors"));
            }
            if (workAuthList.isEmpty()) {
                workAuthList.addAll(DataVars.getAllOfType("Work Authorizations"));
            }
            if (posTypeList.isEmpty()) {
                posTypeList.addAll(DataVars.getAllOfType("Position Types"));
            }
            
            HashMap<String, List<Integer>> categories = new HashMap<String, List<Integer>>();
            categories.put("Majors", majorsList);
            categories.put("Work Authorizations", workAuthList);
            categories.put("Position Types", posTypeList);
            HashMap<String, String> parameters = new HashMap<String, String>();
            
            Company newCompany = new Company(title, categories, null, parameters);
            parameters.put("table", Integer.toString(newCompany.getID() - 99));
            
            companyMap.put(newCompany.getID(), newCompany);
        }
    }
}
