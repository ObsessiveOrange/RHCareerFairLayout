package servlets.data;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import managers.DataManager;
import managers.SQLManager;
import servlets.ServletLog;
import adt.Category;
import adt.Company;
import adt.Layout;
import adt.Response;
import adt.Response.FailResponse;
import adt.Response.SuccessResponse;
import adt.TableMapping;

public class DataRequestHandler {
    
    public static Response handleGetDataRequest(HttpServletRequest request) throws IOException {
    
        try {
            HashMap<String, Object> termVars = new HashMap<String, Object>();
            HashMap<String, HashMap<Integer, Category>> categoryMap = new HashMap<String, HashMap<Integer, Category>>();
            HashMap<Integer, Company> companyMap = new HashMap<Integer, Company>();
            HashMap<String, Object> layoutMap = new HashMap<String, Object>();
            
            // Organize termVars into hashmap
            PreparedStatement getTermVars = SQLManager.getConn(DataManager.getSelectedTerm()).prepareStatement(
                    "SELECT item, value, type FROM TermVars;");
            ResultSet getTermVarsRS = getTermVars.executeQuery();
            
            while (getTermVarsRS.next()) {
                String item = getTermVarsRS.getString("item");
                String value = getTermVarsRS.getString("value");
                String type = getTermVarsRS.getString("type");
                
                if (type.equalsIgnoreCase("layout")) {
                    layoutMap.put(item, Integer.valueOf(value));
                }
                else {
                    termVars.put(item, value);
                }
                
            }
            
            Layout layout = new Layout(layoutMap);
            
            termVars.put("layout", layout);
            
            // Retreive TableMapping from DB
            PreparedStatement getTableMappings = SQLManager.getConn(DataManager.getSelectedTerm()).prepareStatement(
                    "SELECT location, tableNo, tableSize FROM TableMappings;");
            ResultSet getTableMappingsRS = getTableMappings.executeQuery();
            
            while (getTableMappingsRS.next()) {
                Integer location = getTableMappingsRS.getInt("location");
                Integer tableNo = getTableMappingsRS.getInt("tableNo");
                Integer tableSize = getTableMappingsRS.getInt("tableSize");
                TableMapping mapping = new TableMapping(location, tableNo, tableSize);
                
                layout.getLocationTableMapping().put(location, mapping);
                layout.getTableLocationMapping().put(tableNo, mapping);
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
            
        } catch (Exception e) {
            ServletLog.logEvent(e);
            
            return new FailResponse(e);
        }
    }
    
    public static Response handleGetSelectedTermRequest(HttpServletRequest request) throws IOException {
    
        SuccessResponse response = new SuccessResponse();
        response.addToReturnData("selectedQuarter", DataManager.getSelectedQuarter());
        response.addToReturnData("selectedYear", DataManager.getSelectedYear());
        
        return response;
    }
    
    public static Response handleGetCategoriesRequest(HttpServletRequest request) throws IOException {
    
        try {
            HashMap<String, HashMap<Integer, Category>> categoryMap = new HashMap<String, HashMap<Integer, Category>>();
            
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
            
            SuccessResponse response = new SuccessResponse();
            response.addToReturnData("categories", categoryMap);
            
            return response;
        } catch (Exception e) {
            ServletLog.logEvent(e);
            
            return new FailResponse(e);
            
        }
    }
    
    public static Response handleGetCompaniesRequest(HttpServletRequest request) throws IOException {
    
        try {
            
            HashMap<Integer, Company> companyMap = new HashMap<Integer, Company>();
            
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
            response.addToReturnData("companies", companyMap);
            
            return response;
        } catch (Exception e) {
            ServletLog.logEvent(e);
            
            return new FailResponse(e);
            
        }
    }
    
    public static Response handleGetLayoutRequest(HttpServletRequest request) throws IOException {
    
        try {
            
            HashMap<String, Object> layoutMap = new HashMap<String, Object>();
            
            // Organize termVars into hashmap
            PreparedStatement getTermVars = SQLManager.getConn(DataManager.getSelectedTerm()).prepareStatement(
                    "SELECT item, value, type FROM TermVars;");
            ResultSet getTermVarsRS = getTermVars.executeQuery();
            
            while (getTermVarsRS.next()) {
                String item = getTermVarsRS.getString("item");
                String value = getTermVarsRS.getString("value");
                String type = getTermVarsRS.getString("type");
                
                if (type.equalsIgnoreCase("layout")) {
                    layoutMap.put(item, Integer.valueOf(value));
                }
            }
            
            Layout layout = new Layout(layoutMap);
            
            // Retreive TableMapping from DB
            PreparedStatement getTableMappings = SQLManager.getConn(DataManager.getSelectedTerm()).prepareStatement(
                    "SELECT location, tableNo, tableSize FROM TableMappings;");
            ResultSet getTableMappingsRS = getTableMappings.executeQuery();
            
            while (getTableMappingsRS.next()) {
                Integer location = getTableMappingsRS.getInt("location");
                Integer tableNo = getTableMappingsRS.getInt("tableNo");
                Integer tableSize = getTableMappingsRS.getInt("tableSize");
                TableMapping mapping = new TableMapping(location, tableNo, tableSize);
                
                layout.getLocationTableMapping().put(location, mapping);
                layout.getTableLocationMapping().put(tableNo, mapping);
            }
            
            SuccessResponse response = new SuccessResponse();
            response.addToReturnData("layout", layout);
            
            return response;
        } catch (Exception e) {
            ServletLog.logEvent(e);
            
            return new FailResponse(e);
            
        }
    }
    
    public static Response handleGetStatisticsRequest(HttpServletRequest request) throws IOException {
    
        try {
            HashMap<String, Object> layoutMap = new HashMap<String, Object>();
            Layout layout = null;
            Integer mappedTableCount = 0;
            
            // Organize termVars into hashmap
            PreparedStatement getTermVars = SQLManager.getConn(DataManager.getSelectedTerm()).prepareStatement(
                    "SELECT item, value, type FROM TermVars;");
            ResultSet getTermVarsRS = getTermVars.executeQuery();
            
            while (getTermVarsRS.next()) {
                String item = getTermVarsRS.getString("item");
                String value = getTermVarsRS.getString("value");
                String type = getTermVarsRS.getString("type");
                
                if (type.equalsIgnoreCase("layout")) {
                    layoutMap.put(item, Integer.valueOf(value));
                }
            }
            
            layout = new Layout(layoutMap);
            
            HashMap<Integer, Company> companyMap = new HashMap<Integer, Company>();
            
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
            
            PreparedStatement getMappedTables = SQLManager.getConn(DataManager.getSelectedTerm()).prepareStatement(
                    "SELECT SUM(tableSize) as NumTables FROM TableMappings;");
            ResultSet getMappedTablesRS = getMappedTables.executeQuery();
            
            while (getMappedTablesRS.next()) {
                mappedTableCount = getMappedTablesRS.getInt("NumTables");
            }
            
            SuccessResponse response = new SuccessResponse();
            response.addToReturnData("layoutTableCount", layout.getTableCount());
            response.addToReturnData("mappedTableCount", mappedTableCount);
            response.addToReturnData("companyCount", companyMap.size());
            
            return response;
        } catch (Exception e) {
            ServletLog.logEvent(e);
            
            return new FailResponse(e);
            
        }
    }
}