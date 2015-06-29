package servlets.data;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import adt.Category;
import adt.Company;
import adt.Layout;
import adt.TableMapping;
import common.Response;
import common.Response.FailResponse;
import common.Response.SuccessResponse;
import managers.DataManager;
import managers.SQLManager;
import servlets.ServletLog;

public class DataRequestHandler {

    public static Response handleGetDataRequest() throws IOException {

	try {
	    HashMap<String, Object> termVars = new HashMap<String, Object>();
	    HashMap<String, HashMap<Integer, Category>> categoryMap = new HashMap<String, HashMap<Integer, Category>>();
	    HashMap<Integer, Company> companyMap = new HashMap<Integer, Company>();
	    HashMap<String, Object> layoutMap = new HashMap<String, Object>();

	    // Organize termVars into hashmap
	    PreparedStatement getTermVars = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT item, value, type FROM TermVars;");
	    ResultSet getTermVarsRS = getTermVars.executeQuery();

	    while (getTermVarsRS.next()) {
		String item = getTermVarsRS.getString("item");
		String value = getTermVarsRS.getString("value");
		String type = getTermVarsRS.getString("type");

		if (type.equalsIgnoreCase("layout")) {
		    layoutMap.put(item, Integer.valueOf(value));
		} else {
		    termVars.put(item, value);
		}

	    }

	    Layout layout = new Layout(layoutMap);

	    termVars.put("layout", layout);

	    // Retreive TableMapping from DB
	    PreparedStatement getTableMappings = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT tableNumber,companyId, tableSize FROM TableMappings;");
	    ResultSet getTableMappingsRS = getTableMappings.executeQuery();

	    while (getTableMappingsRS.next()) {
		Integer tableNumber = getTableMappingsRS.getInt("tableNumber");
		Integer companyId = getTableMappingsRS.getInt("companyId") == 0 ? null
			: getTableMappingsRS.getInt("companyId");
		Integer tableSize = getTableMappingsRS.getInt("tableSize");
		TableMapping mapping = new TableMapping(tableNumber, companyId, tableSize);

		layout.getTableMappings().add(mapping);
	    }

	    // Organize categories into hashmap
	    PreparedStatement getCategories = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT id, name, type FROM Categories;");
	    ResultSet getCategoriesRS = getCategories.executeQuery();

	    while (getCategoriesRS.next()) {
		Integer id = getCategoriesRS.getInt("id");
		String name = getCategoriesRS.getString("name");
		String type = getCategoriesRS.getString("type");

		Category c = new Category(id, name, type);
		if (categoryMap.get(type) == null) {
		    categoryMap.put(type, new HashMap<Integer, Category>());
		}
		categoryMap.get(type).put(c.getId(), c);
	    }

	    // Organize companies into hashmap
	    PreparedStatement getCompanies = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT id, name, description FROM Companies;");
	    ResultSet getCompaniesRS = getCompanies.executeQuery();

	    while (getCompaniesRS.next()) {
		Integer id = getCompaniesRS.getInt("id");
		String name = getCompaniesRS.getString("name");
		String description = getCompaniesRS.getString("description");

		Company c = new Company(id, name, description, null);
		companyMap.put(c.getId(), c);
	    }

	    // Add categoryIDs to company objects
	    PreparedStatement getCategories_Companies = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT categoryId, companyId FROM Categories_Companies;");
	    ResultSet getCategories_CompaniesRS = getCategories_Companies.executeQuery();

	    while (getCategories_CompaniesRS.next()) {
		Integer categoryId = getCategories_CompaniesRS.getInt("categoryId");
		Integer companyId = getCategories_CompaniesRS.getInt("companyId");

		Company c = companyMap.get(companyId);
		c.getCategories().add(categoryId);
	    }

	    SuccessResponse response = new SuccessResponse();
	    response.put("title", "Career Fair " + termVars.get("Term") + " " + termVars.get("Year"));
	    response.put("categories", categoryMap);
	    response.put("companies", companyMap);
	    response.put("termVars", termVars);

	    return response;

	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    public static Response handleGetSelectedTermRequest() throws IOException {

	SuccessResponse response = new SuccessResponse();
	response.put("selectedQuarter", DataManager.getSelectedQuarter());
	response.put("selectedYear", DataManager.getSelectedYear());

	return response;
    }

    public static Response handleGetCategoriesRequest() throws IOException {

	try {
	    HashMap<String, HashMap<Integer, Category>> categoryMap = new HashMap<String, HashMap<Integer, Category>>();

	    // Organize categories into hashmap
	    PreparedStatement getCategories = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT id, name, type FROM Categories;");
	    ResultSet getCategoriesRS = getCategories.executeQuery();

	    while (getCategoriesRS.next()) {
		Integer id = getCategoriesRS.getInt("id");
		String name = getCategoriesRS.getString("name");
		String type = getCategoriesRS.getString("type");

		Category c = new Category(id, name, type);
		if (categoryMap.get(type) == null) {
		    categoryMap.put(type, new HashMap<Integer, Category>());
		}
		categoryMap.get(type).put(c.getId(), c);
	    }

	    SuccessResponse response = new SuccessResponse();
	    response.put("categories", categoryMap);

	    return response;
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);

	}
    }

    public static Response handleGetCompaniesRequest() throws IOException {

	try {

	    HashMap<Integer, Company> companyMap = new HashMap<Integer, Company>();

	    // Organize companies into hashmap
	    PreparedStatement getCompanies = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT id, name, description FROM Companies;");
	    ResultSet getCompaniesRS = getCompanies.executeQuery();

	    while (getCompaniesRS.next()) {
		Integer id = getCompaniesRS.getInt("id");
		String name = getCompaniesRS.getString("name");
		String description = getCompaniesRS.getString("description");

		Company c = new Company(id, name, description, null);
		companyMap.put(c.getId(), c);
	    }

	    // Add categoryIDs to company objects
	    PreparedStatement getCategories_Companies = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT categoryId, companyId FROM Categories_Companies;");
	    ResultSet getCategories_CompaniesRS = getCategories_Companies.executeQuery();

	    while (getCategories_CompaniesRS.next()) {
		Integer categoryId = getCategories_CompaniesRS.getInt("categoryId");
		Integer companyId = getCategories_CompaniesRS.getInt("companyId");

		Company c = companyMap.get(companyId);
		c.getCategories().add(categoryId);
	    }

	    SuccessResponse response = new SuccessResponse();
	    response.put("companies", companyMap);

	    return response;
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);

	}
    }

    public static Response handleGetLayoutRequest() throws IOException {

	try {

	    HashMap<String, Object> layoutMap = new HashMap<String, Object>();

	    // Organize termVars into hashmap
	    PreparedStatement getTermVars = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT item, value, type FROM TermVars WHERE type='layout';");
	    ResultSet getTermVarsRS = getTermVars.executeQuery();

	    while (getTermVarsRS.next()) {
		String item = getTermVarsRS.getString("item");
		String value = getTermVarsRS.getString("value");

		layoutMap.put(item, Integer.valueOf(value));
	    }

	    Layout layout = new Layout(layoutMap);

	    // Retreive TableMapping from DB
	    PreparedStatement getTableMappings = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT tableNumber, companyId, tableSize FROM TableMappings;");
	    ResultSet getTableMappingsRS = getTableMappings.executeQuery();

	    while (getTableMappingsRS.next()) {
		Integer tableNumber = getTableMappingsRS.getInt("tableNumber");
		Integer companyId = getTableMappingsRS.getInt("companyId") == 0 ? null
			: getTableMappingsRS.getInt("companyId");
		Integer tableSize = getTableMappingsRS.getInt("tableSize");
		TableMapping mapping = new TableMapping(tableNumber, companyId, tableSize);

		layout.getTableMappings().add(mapping);
	    }

	    SuccessResponse response = new SuccessResponse();
	    response.put("layout", layout);

	    return response;
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);

	}
    }

    public static Response handleGetStatisticsRequest() throws IOException {

	try {
	    HashMap<String, Object> layoutMap = new HashMap<String, Object>();
	    Layout layout = null;
	    Integer usedTableCount = 0;

	    // Organize termVars into hashmap
	    PreparedStatement getTermVars = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT item, value, type FROM TermVars;");
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
	    PreparedStatement getCompanies = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT id, name, description FROM Companies;");
	    ResultSet getCompaniesRS = getCompanies.executeQuery();

	    while (getCompaniesRS.next()) {
		Integer id = getCompaniesRS.getInt("id");
		String name = getCompaniesRS.getString("name");
		String description = getCompaniesRS.getString("description");

		Company c = new Company(id, name, description, null);
		companyMap.put(c.getId(), c);
	    }

	    PreparedStatement getMappedTables = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT COUNT(companyId) as NumUsedTables FROM TableMappings;");
	    ResultSet getMappedTablesRS = getMappedTables.executeQuery();

	    while (getMappedTablesRS.next()) {
		usedTableCount = getMappedTablesRS.getInt("NumUsedTables");
	    }

	    SuccessResponse response = new SuccessResponse();
	    response.put("companyCount", companyMap.size());
	    response.put("totalTableCount", layout.getTableCount());
	    response.put("usedTableCount", usedTableCount);

	    return response;
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);

	}
    }
}