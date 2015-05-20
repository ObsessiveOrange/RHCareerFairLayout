package managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import misc.Utils;
import servlets.ServletLog;
import adt.Category;
import adt.Company;
import adt.Response;
import adt.Response.FailResponse;
import adt.Response.SuccessResponse;
import adt.Sheet;

import com.google.gson.Gson;

public class DataManager {
    
    private static String selectedQuarter = null;
    private static String selectedYear    = null;
    
    /**
     * @return the selectedTerm
     */
    public static String getSelectedTerm() {
    
        if (selectedQuarter == null || selectedYear == null) {
            getSelectedTermFromDB();
        }
        return selectedYear + "_" + selectedQuarter;
    }
    
    /**
     * @return the selected quarter, in proper/camel case.
     */
    public static String getSelectedQuarter() {
    
        if (selectedQuarter == null || selectedYear == null) {
            getSelectedTermFromDB();
        }
        return Utils.toProperCase(selectedQuarter);
    }
    
    /**
     * @return the selected year, in proper/camel case.
     */
    public static String getSelectedYear() {
    
        if (selectedQuarter == null || selectedYear == null) {
            getSelectedTermFromDB();
        }
        return selectedYear;
    }
    
    /**
     * @param selectedTerm the selectedTerm to set
     */
    public static void setSelectedTerm(String selectedYear, String selectedQuarter) {
    
        DataManager.selectedQuarter = selectedQuarter;
        DataManager.selectedYear = selectedYear;
        
        try {
            PreparedStatement updateStatement =
                    SQLManager
                            .getConn("RHCareerFairLayout")
                            .prepareStatement(
                                    "INSERT INTO Vars (item, value, type) VALUES (?, ?, ?), (?, ?, ?) ON DUPLICATE KEY UPDATE item=values(item), value=values(value), type=values(type);");
            updateStatement.setString(1, "selectedQuarter");
            updateStatement.setString(2, selectedQuarter);
            updateStatement.setString(3, "selectedTerm");
            updateStatement.setString(4, "selectedYear");
            updateStatement.setString(5, selectedYear);
            updateStatement.setString(6, "selectedTerm");
            updateStatement.executeUpdate();
        } catch (Exception e) {
            ServletLog.logEvent(e);
        }
    }
    
    private static void getSelectedTermFromDB() {
    
        try {
            ResultSet r =
                    SQLManager.getConn("RHCareerFairLayout").createStatement()
                            .executeQuery("SELECT item, value, type FROM Vars WHERE type = 'selectedTerm';");
            
            while (r.next()) {
                if (r.getString("item").equalsIgnoreCase("selectedQuarter")) {
                    selectedQuarter = Utils.toProperCase(r.getString("value"));
                }
                else if (r.getString("item").equalsIgnoreCase("selectedYear")) {
                    selectedYear = r.getString("value");
                }
            }
            
            r.close();
            
        } catch (Exception e) {
            ServletLog.logEvent(e);
        }
    }
    
    public static boolean checkDBExists(String year, String quarter) throws SQLException, ClassNotFoundException {
    
        ResultSet rs = null;
        
        PreparedStatement checkDBExists =
                SQLManager.getConn("RHCareerFairLayout").prepareStatement(
                        "SELECT COUNT(*) AS DBCount FROM Terms WHERE year=? AND quarter=?;");
        checkDBExists.setString(1, year);
        checkDBExists.setString(2, Utils.toProperCase(quarter));
        rs = checkDBExists.executeQuery();
        
        if (rs.next() && rs.getInt("DBCount") > 0) {
            return true;
        }
        return false;
    }
    
    public static Response updateTermVars(String dbName, Sheet termVars) throws SQLException, ClassNotFoundException {
    
        PreparedStatement insertVars =
                SQLManager.getConn(dbName).prepareStatement(
                        "INSERT INTO TermVars (item, value, type) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value=values(value), type=values(type);");
        
        for (int i = 0; i < termVars.getRows(); i++) {
            insertVars.setString(1, termVars.getItem(i, "Item", String.class));
            insertVars.setString(2, termVars.getItem(i, "Value", String.class));
            insertVars.setString(3, termVars.getItem(i, "Type", String.class));
            insertVars.executeUpdate();
        }
        
        return new SuccessResponse();
    }
    
    public static Response updateTableMappings(String dbName, Sheet tableMappings, List<Company> companies) throws SQLException,
            ClassNotFoundException {
    
        PreparedStatement insertTableMapping =
                SQLManager
                        .getConn(dbName)
                        .prepareStatement(
                                "INSERT INTO TableMappings (tableNumber, companyId, tableSize) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE companyId=values(companyId), tableSize=values(tableSize);");
        HashMap<Integer, Integer> tableCompanyMap = new HashMap<Integer, Integer>();
        for (Company c : companies) {
            tableCompanyMap.put(c.getTableNumber(), c.getId());
        }
        System.out.println(new Gson().toJson(tableCompanyMap));
        
        for (int i = 0; i < tableMappings.getRows(); i++) {
            Integer companyID = tableCompanyMap.get(i);
            insertTableMapping.setInt(1, tableMappings.getItem(i, "Table Number", Integer.class));
            if (companyID != null) {
                insertTableMapping.setInt(2, companyID);
            }
            else {
                insertTableMapping.setNull(2, java.sql.Types.INTEGER);
            }
            insertTableMapping.setInt(3, tableMappings.getItem(i, "Table Size", Integer.class));
            insertTableMapping.executeUpdate();
        }
        
        return new SuccessResponse();
    }
    
    public static Response updateCategories(String dbName, List<Category> categories) throws SQLException, ClassNotFoundException {
    
        PreparedStatement insertCategories =
                SQLManager
                        .getConn(dbName)
                        .prepareStatement(
                                "INSERT INTO Categories (id, name, type) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE name=values(name), type=values(type);");
        
        for (Category c : categories) {
            insertCategories.setInt(1, c.getId());
            insertCategories.setString(2, c.getName());
            insertCategories.setString(3, c.getType());
            insertCategories.executeUpdate();
        }
        
        return new SuccessResponse();
    }
    
    public static Response updateCompanies(String dbName, List<Company> companies) throws SQLException, ClassNotFoundException {
    
        PreparedStatement insertCompanies =
                SQLManager
                        .getConn(dbName)
                        .prepareStatement(
                                "INSERT INTO Companies (id, name, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE name=values(name), description=values(description);");
        
        for (Company c : companies) {
            insertCompanies.setInt(1, c.getId());
            insertCompanies.setString(2, c.getName());
            
            if (c.getDescription() != null) {
                insertCompanies.setString(3, c.getDescription());
            }
            else {
                insertCompanies.setNull(3, java.sql.Types.BLOB);
            }
            insertCompanies.executeUpdate();
        }
        
        return new SuccessResponse();
    }
    
    public static Response updateCategories_Companies(String dbName, List<Company> companies) throws SQLException, ClassNotFoundException {
    
        PreparedStatement insertCompanies =
                SQLManager
                        .getConn(dbName)
                        .prepareStatement(
                                "INSERT IGNORE INTO Categories_Companies (categoryId, companyId) VALUES (?, ?)");
        
        for (Company company : companies) {
            for (Integer categoryId : company.getCategories()) {
                insertCompanies.setInt(1, categoryId);
                insertCompanies.setInt(2, company.getId());
                insertCompanies.executeUpdate();
            }
        }
        
        return new SuccessResponse();
    }
    
    public static Response updateCategoriesAndCompanies(String dbName, Sheet categories, Sheet companies) throws SQLException, ClassNotFoundException {
    
        HashMap<String, HashMap<String, Integer>> categoryLookupTable = new HashMap<String, HashMap<String, Integer>>();
        List<Company> companyList = new ArrayList<Company>();
        List<Category> categoryList = new ArrayList<Category>();
        
        for (int i = 0; i < categories.getRows(); i++) {
            String name = categories.getItem(i, "Name", String.class);
            String type = categories.getItem(i, "Type", String.class);
            Category newCategory = new Category(i + 1, name, type);
            
            if (categoryLookupTable.get(type) == null) {
                categoryLookupTable.put(type, new HashMap<String, Integer>());
            }
            
            categoryLookupTable.get(type).put(name, newCategory.getId());
            
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
                if (id != null) {
                    majorsList.add(id);
                }
            }
            
            for (String workAuth : workAuths) {
                Integer id = categoryLookupTable.get("Work Authorization").get(workAuth.trim());
                if (id != null) {
                    workAuthList.add(id);
                }
            }
            
            for (String posType : posTypes) {
                Integer id = categoryLookupTable.get("Position Type").get(posType.trim());
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
            
            List<Integer> companyCategories = new ArrayList<Integer>();
            companyCategories.addAll(majorsList);
            companyCategories.addAll(workAuthList);
            companyCategories.addAll(posTypeList);
            Integer tableNumber = companies.getItem(i, "Table Number", Integer.class);
            
            Company newCompany = new Company(i + 100, name, companyCategories, null, tableNumber);
            companyList.add(newCompany);
        }
        
        Response updateCategoriesResponse = updateCategories(dbName, categoryList);
        Response updateCompaniesResponse = updateCompanies(dbName, companyList);
        Response updateCategories_CompaniesResponse = updateCategories_Companies(dbName, companyList);
        if (updateCategoriesResponse.success && updateCompaniesResponse.success && updateCategories_CompaniesResponse.success) {
            SuccessResponse response = new SuccessResponse();
            response.addToReturnData("companyList", companyList);
            return response;
        }
        
        Response failed = new FailResponse(-1);
        failed.addToReturnData("updateCategoriesResponse", updateCompaniesResponse);
        failed.addToReturnData("updateCompaniesResponse", updateCompaniesResponse);
        failed.addToReturnData("updateCategories_CompaniesResponse", updateCategories_CompaniesResponse);
        return failed;
        
    }
}
