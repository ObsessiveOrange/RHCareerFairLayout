package servlets;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import adt.Category;
import adt.Company;
import adt.Layout;
import adt.Response;
import adt.Response.FailResponse;
import adt.Response.SuccessResponse;
import adt.TableMapping;
import managers.DataManager;
import managers.SQLManager;

@Path("/api2/data")
public class Data {

    @GET
    @Path("get_data")
    public Response getData() {

	try {
	    HashMap<String, Object> termVars = new HashMap<String, Object>();

	    // Organize termVars into hashmap
	    PreparedStatement getTermVars;
	    getTermVars = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT item, value FROM TermVars WHERE type != 'layout';");
	    ResultSet getTermVarsRS = getTermVars.executeQuery();

	    while (getTermVarsRS.next()) {
		String item = getTermVarsRS.getString("item");
		String value = getTermVarsRS.getString("value");

		termVars.put(item, value);
	    }

	    SuccessResponse response = new SuccessResponse();
	    response.addToReturnData("title", "Career Fair " + termVars.get("Term") + " " + termVars.get("Year"));
	    response.addToReturnData("categoryList", getCategoryList().getFromReturnData("categoryList"));
	    response.addToReturnData("companyList", getCompanyList().getFromReturnData("companyList"));
	    response.addToReturnData("layout", getLayout().getFromReturnData("layout"));

	    return response;

	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    @GET
    @Path("get_selected_term")
    public Response getSelectedTerm() {
	try {
	    HashMap<String, Object> termVars = new HashMap<String, Object>();

	    // Organize termVars into hashmap
	    PreparedStatement getTermVars;
	    getTermVars = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT item, value FROM TermVars WHERE type != 'layout';");
	    ResultSet getTermVarsRS = getTermVars.executeQuery();

	    while (getTermVarsRS.next()) {
		String item = getTermVarsRS.getString("item");
		String value = getTermVarsRS.getString("value");

		termVars.put(item, value);
	    }

	    SuccessResponse response = new SuccessResponse();
	    response.addToReturnData("title", "Career Fair " + termVars.get("Term") + " " + termVars.get("Year"));
	    response.addToReturnData("title", "Career Fair " + termVars.get("Term") + " " + termVars.get("Year"));

	    return response;

	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    @GET
    @Path("get_category_list")
    public Response getCategoryList() {

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
	    response.addToReturnData("categories", categoryMap);

	    return response;
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);

	}
    }

    @GET
    @Path("get_company_list")
    public Response getCompanyList() {

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
	    response.addToReturnData("companies", companyMap);

	    return response;
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);

	}
    }

    @GET
    @Path("get_company_details/{companyId}")
    public Response getCompanyDetails(@PathParam("companyId") String companyIdStr) {

	try {
	    int companyId = Integer.valueOf(companyIdStr);

	    Company company = null;

	    // Organize companies into hashmap
	    PreparedStatement getCompany = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT id, name, description FROM Companies WHERE id = ?;");
	    getCompany.setInt(1, companyId);
	    ResultSet getCompanyRS = getCompany.executeQuery();

	    if (getCompanyRS.next()) {
		Integer id = getCompanyRS.getInt("id");
		String name = getCompanyRS.getString("name");
		String description = getCompanyRS.getString("description");

		company = new Company(id, name, description, null);
	    } else {
		return new FailResponse("Invalid companyId provided: No results found");
	    }

	    // Add categoryIDs to company objects
	    PreparedStatement getCategories = SQLManager.getConn(DataManager.getSelectedTerm())
		    .prepareStatement("SELECT categoryId FROM Categories_Companies WHERE companyId = ?;");
	    getCategories.setInt(1, companyId);
	    ResultSet getCategoriesRS = getCategories.executeQuery();

	    if (getCategoriesRS.next()) {
		Integer categoryId = getCategoriesRS.getInt("categoryId");

		company.getCategories().add(categoryId);
	    } else {
		return new FailResponse("Invalid companyId provided: No results found");
	    }

	    SuccessResponse response = new SuccessResponse();
	    response.addToReturnData("companyDetails", company);

	    return response;
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);

	}
    }

    @GET
    @Path("get_layout")
    public Response getLayout() {

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
	    response.addToReturnData("layout", layout);

	    return response;
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);

	}
    }

    @GET
    @Path("get_statistics")
    public Response getStatistics() {

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
	    response.addToReturnData("companyCount", companyMap.size());
	    response.addToReturnData("totalTableCount", layout.getTableCount());
	    response.addToReturnData("usedTableCount", usedTableCount);

	    return response;
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);

	}
    }
}
