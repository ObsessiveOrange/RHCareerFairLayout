package servlets;

import java.sql.CallableStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import adt.Category;
import adt.Company;
import adt.DataVar;
import adt.Table;
import adt.wrappers.CategoryMap;
import adt.wrappers.CompanyMap;
import adt.wrappers.DataVarMap;
import adt.wrappers.TableArray;
import common.Response;
import common.Response.FailResponse;
import common.Response.SuccessResponse;
import managers.DataManager;
import managers.SQLManager;
import misc.Utils;

@Path("/data")
public class Data {

    @Context
    private HttpServletResponse response;

    @GET
    @Produces("application/json")
    @Path("all")
    public Response get_AllData() {

	try {

	    SuccessResponse response = new SuccessResponse();

	    Response r;
	    r = get_SelectedTerm();
	    if (!r.isSuccess()) {
		return r;
	    } else {
		response.put("title", r.get("title"));
	    }

	    r = get_CategoryList();
	    if (!r.isSuccess()) {
		return r;
	    } else {
		response.put("categoryList", r.get("categoryList"));
	    }

	    r = get_CompanyList();
	    if (!r.isSuccess()) {
		return r;
	    } else {
		response.put("companyList", r.get("companyList"));
	    }

	    r = get_Layout();
	    if (!r.isSuccess()) {
		return r;
	    } else {
		response.put("layoutVars", r.get("layoutVars"));
		response.put("tableMappings", r.get("tableMappings"));
	    }

	    return response;

	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    @POST
    @Consumes("multipart/form-data")
    @Produces("application/json")
    @Path("all")
    public Response post_AllData() {

	try {

	    // TODO: Implement
	    return new FailResponse("Not implemented yet");

	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    @GET
    @Produces("application/json")
    @Path("selected_term")
    public Response get_SelectedTerm() {
	try {
	    CallableStatement stmt;
	    ResultSet rs;
	    Response respObj;

	    // Check session is valid
	    stmt = SQLManager.getConn(DataManager.getSelectedTerm()).prepareCall("CALL Data_Get_SelectedTerm();");

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs)).isSuccess()) {
		return respObj;
	    }

	    String quarter = "", year = "";
	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		if (Utils.hasColumn(rs, "item")) {
		    while (rs.next()) {
			if (rs.getString("item").equalsIgnoreCase("Term_Quarter")) {
			    quarter = rs.getString("value");
			} else if (rs.getString("item").equalsIgnoreCase("Term_Year")) {
			    year = rs.getString("value");
			}
		    }
		}
	    }

	    SuccessResponse response = new SuccessResponse();
	    response.put("title", "Career Fair " + quarter + " " + year);

	    return response;

	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    @GET
    @Produces("application/json")
    @Path("category_list")
    public Response get_CategoryList() {
	try {
	    CallableStatement stmt;
	    ResultSet rs;
	    Response respObj;

	    // Check session is valid
	    stmt = SQLManager.getConn(DataManager.getSelectedTerm()).prepareCall("CALL Data_Get_CategoryList();");

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs)).isSuccess()) {
		return respObj;
	    }

	    CategoryMap categoryMap = new CategoryMap();
	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {
		    Category c = new Category(rs.getLong("id"), rs.getString("name"), rs.getString("type"));

		    categoryMap.put(c.getId(), c);
		}
	    }

	    SuccessResponse response = new SuccessResponse();
	    response.put("categoryList", categoryMap);

	    return response;

	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    @GET
    @Produces("application/json")
    @Path("company_list")
    public Response get_CompanyList() {
	try {
	    CallableStatement stmt;
	    ResultSet rs;
	    Response respObj;

	    // Check session is valid
	    stmt = SQLManager.getConn(DataManager.getSelectedTerm()).prepareCall("CALL Data_Get_CompanyList();");

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs)).isSuccess()) {
		return respObj;
	    }

	    CompanyMap companyMap = new CompanyMap();
	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {
		    Company c = new Company(rs.getLong("id"), rs.getString("name"), rs.getString("description"),
			    rs.getLong("tableId"));

		    companyMap.put(c.getId(), c);
		}
	    }

	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {
		    companyMap.get(rs.getLong("companyId")).getCategories().add(rs.getLong("categoryId"));
		}
	    }

	    SuccessResponse response = new SuccessResponse();
	    response.put("companyList", companyMap);

	    return response;

	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    @GET
    @Produces("application/json")
    @Path("company_details/{companyId}")
    public Response get_CompanyDetails(@PathParam("companyId") String companyIdStr) {

	try {
	    Long companyId;
	    try {
		companyId = Long.valueOf(companyIdStr);
	    } catch (NumberFormatException e) {
		return new FailResponse("Company ID invalid - non-integer value");
	    }

	    CallableStatement stmt;
	    ResultSet rs;
	    Response respObj;

	    // Check session is valid
	    stmt = SQLManager.getConn(DataManager.getSelectedTerm()).prepareCall("CALL Data_Get_CompanyDetails(?);");
	    stmt.setLong(1, companyId);

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs)).isSuccess()) {
		return respObj;
	    }

	    Company company = null;
	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {
		    company = new Company(rs.getLong("id"), rs.getString("name"), rs.getString("description"),
			    rs.getLong("tableId"));
		}
		if (company == null) {
		    return new FailResponse("Error retreiving company instance");
		}
	    }

	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {
		    company.getCategories().add(rs.getLong("categoryId"));
		}
	    }

	    SuccessResponse response = new SuccessResponse();
	    response.put("company", company);

	    return response;
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    @GET
    @Produces("application/json")
    @Path("layout")
    public Response get_Layout() {

	try {

	    CallableStatement stmt;
	    ResultSet rs;
	    Response respObj;

	    DataVarMap layout = new DataVarMap();
	    TableArray tableMappings = new TableArray();

	    // Check session is valid
	    stmt = SQLManager.getConn(DataManager.getSelectedTerm()).prepareCall("CALL Data_Get_LayoutVars();");

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs)).isSuccess()) {
		return respObj;
	    }

	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {
		    DataVar var = new DataVar(rs.getString("item"), rs.getString("value"));

		    layout.put(var);
		}
	    }

	    stmt = SQLManager.getConn(DataManager.getSelectedTerm()).prepareCall("CALL Data_Get_TableMappings();");

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs)).isSuccess()) {
		return respObj;
	    }

	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {
		    Long tableId = rs.getLong("tableId") == 0 && rs.wasNull() ? null : rs.getLong("tableId");
		    Long companyId = rs.getLong("companyId") == 0 && rs.wasNull() ? null : rs.getLong("companyId");
		    Integer tableSize = rs.getInt("tableSize") == 0 && rs.wasNull() ? null : rs.getInt("tableSize");

		    Table table = new Table(tableId, companyId, tableSize);

		    tableMappings.add(table);
		}
	    }

	    SuccessResponse response = new SuccessResponse();
	    response.put("layoutVars", layout);
	    response.put("tableMappings", tableMappings);

	    return response;
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);

	}
    }

    @GET
    @Produces("application/json")
    @Path("statistics")
    public Response get_Statistics() {

	try {

	    CallableStatement stmt;
	    ResultSet rs;
	    Response respObj;

	    Integer companyCount = null, totalTableCount = null, usedTableCount = null;

	    // Check session is valid
	    stmt = SQLManager.getConn(DataManager.getSelectedTerm()).prepareCall("CALL Data_Get_Statistics();");

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs)).isSuccess()) {
		return respObj;
	    }

	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		if (rs.next()) {

		    companyCount = rs.getInt("companyCount");
		    totalTableCount = rs.getInt("totalTableCount");
		    usedTableCount = rs.getInt("usedTableCount");
		}
	    }

	    SuccessResponse response = new SuccessResponse();
	    response.put("companyCount", companyCount);
	    response.put("totalTableCount", totalTableCount);
	    response.put("usedTableCount", usedTableCount);

	    return response;
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}

    }
}
