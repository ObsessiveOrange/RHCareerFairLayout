package managers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import servlets.ServletLog;
import servlets.ServletLog.ServletEvent;
import adt.Category;
import adt.Company;
import adt.Response;
import adt.Response.FailResponse;
import adt.Response.SuccessResponse;
import adt.Sheet;

public class DataManager {
    
    public static Response updateTermVars(String dbName, Sheet termVars) throws SQLException {
    
        PreparedStatement insertVars =
                SQLManager.getConn(dbName).prepareStatement(
                        "INSERT INTO TermVars (item, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value=values(value);");
        
        for (int i = 0; i < termVars.getRows(); i++) {
            insertVars.setString(1, termVars.getItem(i, "Item", String.class));
            insertVars.setString(2, termVars.getItem(i, "Value", String.class));
            insertVars.executeUpdate();
        }
        
        return new SuccessResponse();
    }
    
    public static Response updateTableMappings(String dbName, Sheet tableMappings) throws SQLException {
    
        PreparedStatement insertTableMapping =
                SQLManager
                        .getConn(dbName)
                        .prepareStatement(
                                "INSERT INTO TableMappings (location, tableNo, tableSize) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE tableNo=values(tableNo), tableSize=values(tableSize);");
        
        for (int i = 0; i < tableMappings.getRows(); i++) {
            insertTableMapping.setInt(1, tableMappings.getItem(i, "Location", Integer.class));
            insertTableMapping.setInt(2, tableMappings.getItem(i, "Table Number", Integer.class));
            insertTableMapping.setInt(3, tableMappings.getItem(i, "Table Size", Integer.class));
            insertTableMapping.executeUpdate();
        }
        
        return new SuccessResponse();
    }
    
    public static Response updateCategories(String dbName, List<Category> categories) throws SQLException {
    
        PreparedStatement insertCategories =
                SQLManager
                        .getConn(dbName)
                        .prepareStatement(
                                "INSERT INTO Categories (id, name, type) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE name=values(name), type=values(type);");
        
        for (Category c : categories) {
            insertCategories.setInt(1, c.getID());
            insertCategories.setString(2, c.getName());
            insertCategories.setString(3, c.getType());
            insertCategories.executeUpdate();
        }
        
        return new SuccessResponse();
    }
    
    public static Response updateCompanies(String dbName, List<Company> companies) throws SQLException {
    
        PreparedStatement insertCompanies =
                SQLManager
                        .getConn(dbName)
                        .prepareStatement(
                                "INSERT INTO Companies (id, name, tableNo, description) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=values(name), tableNo=values(tableNo), description=values(description);");
        
        for (Company c : companies) {
            insertCompanies.setInt(1, c.getID());
            insertCompanies.setString(2, c.getName());
            
            if (c.getTableNumber() != null) {
                insertCompanies.setInt(3, c.getTableNumber());
            }
            else {
                insertCompanies.setNull(3, java.sql.Types.INTEGER);
            }
            
            if (c.getDescription() != null) {
                insertCompanies.setString(4, c.getDescription());
            }
            else {
                insertCompanies.setNull(4, java.sql.Types.BLOB);
            }
            insertCompanies.executeUpdate();
        }
        
        return new SuccessResponse();
    }
    
    public static Response updateCategories_Companies(String dbName, List<Company> companies) throws SQLException {
    
        PreparedStatement insertCompanies =
                SQLManager
                        .getConn(dbName)
                        .prepareStatement(
                                "INSERT INTO Categories_Companies (categoryId, companyId) VALUES (?, ?) ON DUPLICATE KEY UPDATE categoryId=values(categoryId), companyId=values(companyId);");
        
        for (Company company : companies) {
            for (Integer categoryID : company.getCategories()) {
                insertCompanies.setInt(1, categoryID);
                insertCompanies.setInt(2, company.getID());
                insertCompanies.executeUpdate();
            }
        }
        
        return new SuccessResponse();
    }
    
    public static Response updateCategoriesAndCompanies(String dbName, Sheet categories, Sheet companies) throws SQLException {
    
        HashMap<String, HashMap<String, Integer>> categoryLookupTable = new HashMap<String, HashMap<String, Integer>>();
        List<Company> companyList = new ArrayList<Company>();
        List<Category> categoryList = new ArrayList<Category>();
        
        int test = 0;
        
        for (int i = 0; i < categories.getRows(); i++) {
            String name = categories.getItem(i, "Name", String.class);
            String type = categories.getItem(i, "Type", String.class);
            Category newCategory = new Category(name, type);
            
            if (categoryLookupTable.get(type) == null) {
                categoryLookupTable.put(type, new HashMap<String, Integer>());
            }
            
            categoryLookupTable.get(type).put(name, newCategory.getID());
            
            categoryList.add(newCategory);
        }
        
        for (int i = 0; i < companies.getRows(); i++) {
            String name = companies.getItem(i, 0, String.class).replace("\"", "");
            String[] majors = companies.getItem(i, 1, String.class) == null ? new String[] {} : companies.getItem(i, 1, String.class).split(",");
            String[] workAuths =
                    companies.getItem(i, 2, String.class) == null ? new String[] {} : companies.getItem(i, 2, String.class).split(",");
            String[] posTypes =
                    companies.getItem(i, 3, String.class) == null ? new String[] {} : companies.getItem(i, 3, String.class).split(",");
            
            ArrayList<Integer> majorsList = new ArrayList<Integer>();
            ArrayList<Integer> workAuthList = new ArrayList<Integer>();
            ArrayList<Integer> posTypeList = new ArrayList<Integer>();
            
            for (String major : majors) {
                Integer id = categoryLookupTable.get("Major").get(major.trim());
                if (test < 1) {
                    
                    ServletEvent e = new ServletEvent();
                    e.setDetail("majorId", id);
                    ServletLog.logEvent(e);
                }
                if (id != null) {
                    majorsList.add(id);
                }
            }
            
            for (String workAuth : workAuths) {
                Integer id = categoryLookupTable.get("Work Authorization").get(workAuth.trim());
                if (test < 1) {
                    
                    ServletEvent e = new ServletEvent();
                    e.setDetail("workAuthId", id);
                    ServletLog.logEvent(e);
                }
                if (id != null) {
                    workAuthList.add(id);
                }
            }
            
            for (String posType : posTypes) {
                Integer id = categoryLookupTable.get("Position Type").get(posType.trim());
                if (test < 1) {
                    
                    ServletEvent e = new ServletEvent();
                    e.setDetail("posTypeId", id);
                    ServletLog.logEvent(e);
                }
                if (id != null) {
                    posTypeList.add(id);
                }
            }
            
            if (majorsList.isEmpty()) {
                majorsList.addAll(categoryLookupTable.get("Major").values());
            }
            if (workAuthList.isEmpty()) {
                workAuthList.addAll(categoryLookupTable.get("Work Authorization").values());
            }
            if (posTypeList.isEmpty()) {
                posTypeList.addAll(categoryLookupTable.get("Position Type").values());
            }
            
            if (test < 1) {
                
                ServletEvent e = new ServletEvent();
                e.setDetail("majorsList", majorsList);
                e.setDetail("workAuthList", workAuthList);
                e.setDetail("posTypeList", posTypeList);
                ServletLog.logEvent(e);
                test++;
            }
            
            List<Integer> companyCategories = new ArrayList<Integer>();
            companyCategories.addAll(majorsList);
            companyCategories.addAll(workAuthList);
            companyCategories.addAll(posTypeList);
            Integer tableNumber = companies.getItem(i, "Table", Integer.class);
            
            Company newCompany = new Company(name, companyCategories, null, tableNumber);
            companyList.add(newCompany);
        }
        
        Response updateCategoriesResponse = updateCategories(dbName, categoryList);
        Response updateCompaniesResponse = updateCompanies(dbName, companyList);
        Response updateCategories_CompaniesResponse = updateCategories_Companies(dbName, companyList);
        if (updateCategoriesResponse.success && updateCompaniesResponse.success && updateCategories_CompaniesResponse.success) {
            return new SuccessResponse();
        }
        
        Response failed = new FailResponse(-1);
        failed.addToReturnData("updateCategoriesResponse", updateCompaniesResponse);
        failed.addToReturnData("updateCompaniesResponse", updateCompaniesResponse);
        failed.addToReturnData("updateCategories_CompaniesResponse", updateCategories_CompaniesResponse);
        return failed;
        
    }
}
