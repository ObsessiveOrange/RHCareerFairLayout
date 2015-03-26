package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import misc.ArrayList2D;
import objects.Category;
import objects.Company;
import objects.DataVars;
import objects.LayoutVars;
import objects.SystemVars;

import com.google.gson.Gson;

public class RHCareerFairLayout extends HttpServlet {
    
    /**
     * 
     */
    private static final long                serialVersionUID = -5982008108929904358L;
    
    public static DataVars                   dataVars;
    public static LayoutVars                 layoutVars;
    public static SystemVars                 systemVars;
    public static HashMap<Integer, Category> categoryMap      = new HashMap<Integer, Category>();
    public static HashMap<Integer, Company>  entryMap         = new HashMap<Integer, Company>();
    
    public static boolean                    isTest           = true;
    
    /** Getter & Setter Methods **/
    
    public RHCareerFairLayout() throws IOException {
    
        super();
        dataVars = new DataVars();
        
        setupTestData();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
        response.setContentType("text/plain");
        String method = request.getParameter("method") != null ? request.getParameter("method") : "null";
        
        String responseString;
        
        switch (method) {
            case "getData":
                responseString = RequestHandler.handleGetDataRequest(request);
                break;
            case "start":
                responseString = RequestHandler.handleStartRequest(request);
                break;
            case "testDB":
                responseString = RequestHandler.handleTestDbRequest(request);
                break;
            case "test":
                Map<String, String> env = System.getenv();
                Properties prop = System.getProperties();
                
                HashMap<String, Object> returnMap = new HashMap<String, Object>();
                
                returnMap.put("success", 0);
                returnMap.put("env", env);
                returnMap.put("prop", prop.entrySet());
                returnMap.put("timestamp", System.currentTimeMillis());
                
                responseString = new Gson().toJson(returnMap);
                break;
            // case "getUniqueID":
            // response.getWriter().print(RequestHandler.handleGetUniqueIDRequest(request));
            // break;
            // case "getCartItems":
            // response.getWriter().print(RequestHandler.handleGetCartItemsRequest(request));
            // break;
            // case "getAllCarts":
            // response.getWriter().print(RequestHandler.handleGetAllCartsRequest(request));
            // break;
            // case "getAllOrders":
            // response.getWriter().print(RequestHandler.handleGetAllOrdersRequest(request));
            // break;
            // case "null":
            default:
                responseString =
                        "{\"timestamp\":" + System.currentTimeMillis() + ", \"success\":0, \"error\":\"Invalid GET method supplied: " + method
                                + "\"}";
                break;
        }
        response.getWriter().print(responseString);
        System.out.println(responseString);
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
        response.setContentType("text/plain");
        
        String method = request.getParameter("method") != null ? request.getParameter("method") : "null";
        
        String responseString;
        
        switch (method) {
            case "setSize":
                responseString = RequestHandler.handleSetSizeRequest(request);
                break;
            case "forceRegenerationOfData":
                responseString = RequestHandler.handleForceRegenerationOfData(request);
                break;
            // case "resetTestData":
            // response.getWriter().print(RequestHandler.resetTestData(request));
            // break;
            // case "submitOrder":
            // response.getWriter().print(RequestHandler.handleSubmitOrderRequest(request));
            // break;
            default:
                responseString =
                        "{\"timestamp\":" + System.currentTimeMillis() + ", \"success\":0, \"error\":\"Invalid POST method supplied: " + method
                                + "\"}";
                break;
        }
        response.getWriter().print(responseString);
        System.out.println(responseString);
    }
    
    public static void setupTestData() throws IOException {
    
        setupBasicData();
        
        setupCategories();
        
        setupCompanyData();
    }
    
    private static void setupBasicData() throws IOException {
    
        ArrayList2D basicData = new ArrayList2D();
        basicData.importFromResourceFile("BasicData.txt", "\t", true);
        dataVars.setYear(basicData.getItem("Year", "Value", Integer.class));
        dataVars.setQuarter(basicData.getItem("Quarter", "Value", String.class));
        
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
            
            dataVars.addToIDLookupTable(title, type, newCategory.getID());
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
                Integer id = dataVars.getFromIDLookupTable("Majors", major);
                if (id != null) {
                    majorsList.add(dataVars.getFromIDLookupTable("Majors", major));
                }
            }
            
            for (String workAuth : workAuths) {
                Integer id = dataVars.getFromIDLookupTable("Work Authorizations", workAuth);
                if (id != null) {
                    workAuthList.add(dataVars.getFromIDLookupTable("Work Authorizations", workAuth));
                }
            }
            
            for (String posType : posTypes) {
                Integer id = dataVars.getFromIDLookupTable("Position Types", posType);
                if (id != null) {
                    posTypeList.add(dataVars.getFromIDLookupTable("Position Types", posType));
                }
            }
            
            if (majorsList.isEmpty()) {
                majorsList.addAll(dataVars.getAllOfType("Majors"));
            }
            if (workAuthList.isEmpty()) {
                workAuthList.addAll(dataVars.getAllOfType("Work Authorizations"));
            }
            if (posTypeList.isEmpty()) {
                posTypeList.addAll(dataVars.getAllOfType("Position Types"));
            }
            
            HashMap<String, List<Integer>> categories = new HashMap<String, List<Integer>>();
            categories.put("Majors", majorsList);
            categories.put("Work Authorizations", workAuthList);
            categories.put("Position Types", posTypeList);
            HashMap<String, String> parameters = new HashMap<String, String>();
            
            Company newEntity = new Company(title, categories, null, parameters);
            parameters.put("table", Integer.toString(newEntity.getID() - 99));
            
            entryMap.put(newEntity.getID(), newEntity);
            
            synchronized (dataVars) {
                dataVars.addToIDLookupTable(title, "company", newEntity.getID());
            }
        }
    }
}
