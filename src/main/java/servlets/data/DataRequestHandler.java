package servlets.data;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import managers.DataManager;
import managers.SQLManager;
import servlets.ServletLog;
import servlets.ServletLog.ServletEvent;
import adt.Category;
import adt.Company;
import adt.Response;
import adt.Response.FailResponse;
import adt.Response.SuccessResponse;
import adt.TableMapping;

public class DataRequestHandler {
    
    @SuppressWarnings("unchecked")
    public static Response handleGetDataRequest(HttpServletRequest request) throws IOException {
    
        try {
            HashMap<String, Object> termVars = new HashMap<String, Object>();
            HashMap<String, HashMap<Integer, Category>> categoryMap = new HashMap<String, HashMap<Integer, Category>>();
            HashMap<Integer, Company> companyMap = new HashMap<Integer, Company>();
            HashMap<String, Object> layout = new HashMap<String, Object>();
            
            // Organize termVars into hashmap
            PreparedStatement getTermVars = SQLManager.getConn(DataManager.getSelectedTerm()).prepareStatement(
                    "SELECT item, value, type FROM TermVars;");
            ResultSet getTermVarsRS = getTermVars.executeQuery();
            
            while (getTermVarsRS.next()) {
                String item = getTermVarsRS.getString("item");
                String value = getTermVarsRS.getString("value");
                String type = getTermVarsRS.getString("type");
                
                if (type.equalsIgnoreCase("layout")) {
                    layout.put(item.replace("Layout_", ""), Integer.valueOf(value));
                }
                else {
                    termVars.put(item, value);
                }
                
            }
            
            termVars.put("layout", layout);
            
            // Retreive TableMapping from DB
            PreparedStatement getTableMappings = SQLManager.getConn(DataManager.getSelectedTerm()).prepareStatement(
                    "SELECT location, tableNo, tableSize FROM TableMappings;");
            ResultSet getTableMappingsRS = getTableMappings.executeQuery();
            
            layout.put("locationTableMapping", new HashMap<Integer, TableMapping>());
            layout.put("tableLocationMapping", new HashMap<Integer, TableMapping>());
            
            while (getTableMappingsRS.next()) {
                Integer location = getTableMappingsRS.getInt("location");
                Integer tableNo = getTableMappingsRS.getInt("tableNo");
                Integer tableSize = getTableMappingsRS.getInt("tableSize");
                TableMapping mapping = new TableMapping(location, tableNo, tableSize);
                
                ((HashMap<Integer, TableMapping>) layout.get("locationTableMapping")).put(location, mapping);
                ((HashMap<Integer, TableMapping>) layout.get("tableLocationMapping")).put(tableNo, mapping);
            }
            
            // Organize categories into hashmap
            PreparedStatement getCategories = SQLManager.getConn(DataManager.getSelectedTerm()).prepareStatement(
                    "SELECT id, name, type FROM Categories;");
            ResultSet getCategoriesRS = getCategories.executeQuery();
            
            while (getCategoriesRS.next()) {
                Integer id = getCategoriesRS.getInt("id");
                String name = getCategoriesRS.getString("name");
                String type = getCategoriesRS.getString("type");
                
                Category c = new Category(id, name, type);
                if (categoryMap.get(type) == null) {
                    categoryMap.put(type, new HashMap<Integer, Category>());
                }
                categoryMap.get(type).put(c.getID(), c);
            }
            
            // Organize companies into hashmap
            PreparedStatement getCompanies = SQLManager.getConn(DataManager.getSelectedTerm()).prepareStatement(
                    "SELECT id, name, tableNo, description FROM Companies;");
            ResultSet getCompaniesRS = getCompanies.executeQuery();
            
            while (getCompaniesRS.next()) {
                Integer id = getCompaniesRS.getInt("id");
                String name = getCompaniesRS.getString("name");
                String description = getCompaniesRS.getString("description");
                Integer tableNo = getCompaniesRS.getInt("tableNo");
                
                Company c = new Company(id, name, description, tableNo);
                companyMap.put(c.getID(), c);
            }
            
            // Add categoryIDs to company objects
            PreparedStatement getCategories_Companies = SQLManager.getConn(DataManager.getSelectedTerm()).prepareStatement(
                    "SELECT categoryId, companyId FROM Categories_Companies;");
            ResultSet getCategories_CompaniesRS = getCategories_Companies.executeQuery();
            
            while (getCategories_CompaniesRS.next()) {
                Integer categoryId = getCategories_CompaniesRS.getInt("categoryId");
                Integer companyId = getCategories_CompaniesRS.getInt("companyId");
                
                Company c = companyMap.get(companyId);
                c.getCategories().add(categoryId);
            }
            
            SuccessResponse response = new SuccessResponse();
            response.addToReturnData("title", "Career Fair " + termVars.get("Term") + " "
                    + termVars.get("Year"));
            response.addToReturnData("categories", categoryMap);
            response.addToReturnData("companies", companyMap);
            response.addToReturnData("termVars", termVars);
            
            return response;
            
        } catch (SQLException e) {
            ServletEvent event = new ServletEvent();
            event.setDetail("Type", "Exception");
            event.setDetail("Exception", e.getStackTrace());
            ServletLog.logEvent(event);
            
            return new FailResponse(e);
        }
    }
    
    public static Response handleGetSelectedTermRequest(HttpServletRequest request) throws IOException {
    
        SuccessResponse response = new SuccessResponse();
        response.addToReturnData("selectedQuarter", DataManager.getSelectedQuarter());
        response.addToReturnData("selectedYear", DataManager.getSelectedYear());
        
        return response;
    }
}