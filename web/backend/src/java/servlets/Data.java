package servlets;

import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import adt.models.Category;
import adt.models.Company;
import adt.models.CompanyCategory;
import adt.models.Sheet;
import adt.models.TableMapping;
import adt.models.Term;
import adt.models.Workbook;
import adt.models.wrappers.CategoryMap;
import adt.models.wrappers.CompanyCategoryMap;
import adt.models.wrappers.CompanyMap;
import adt.models.wrappers.TableMappingArray;
import adt.models.wrappers.TermArray;
import common.Result;
import common.Result.FailResult;
import common.Result.SuccessResult;
import managers.SQLManager;
import misc.Utils;

@Path("/data")
public class Data {

    @Context
    private HttpServletResponse response;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/all/latest")
    @Deprecated
    public Response get_AllData_Latest() {
	return get_AllData("latest");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{termId}/all")
    public Response get_AllData(@PathParam("termId") String termIdString) {

	SuccessResult response = new SuccessResult();

	Result getTermResult = getTerm(termIdString);
	if (!getTermResult.isSuccess()) {
	    return getTermResult.toJAXRS();
	}
	Term term = getTermResult.get("term", Term.class);
	response.put("term", term);

	try {

	    Result r;

	    r = CategoryMap.getCategories();
	    if (!r.isSuccess()) {
		return r.toJAXRS();
	    } else {
		response.put("categoryMap", r.get("categoryMap"));
	    }

	    r = CompanyCategoryMap.getCompanyCategories(term.getId());
	    if (!r.isSuccess()) {
		return r.toJAXRS();
	    } else {
		response.put("companyCategoryMap", r.get("companyCategoryMap"));
	    }

	    r = CompanyMap.getCompanies(term.getId());
	    if (!r.isSuccess()) {
		return r.toJAXRS();
	    } else {
		response.put("companyMap", r.get("companyMap"));
	    }

	    r = TableMappingArray.getTableMappings(term.getId());
	    if (!r.isSuccess()) {
		return r.toJAXRS();
	    } else {
		response.put("tableMappingList", r.get("tableMappingList"));
	    }

	    return response.toJAXRS();

	} catch (Exception e) {
	    e.printStackTrace();
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();
	}
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("new/all")
    public Response post_AllData(@CookieParam("authUser") String authUser, @CookieParam("authToken") String authToken,
	    @HeaderParam("User-Agent") String userAgent, @FormDataParam("data") InputStream fileInputStream,
	    @FormDataParam("data") FormDataContentDisposition fileDetail, @FormDataParam("year") Integer year,
	    @FormDataParam("quarter") String quarter,
	    @DefaultValue("false") @FormDataParam("setTermActive") Boolean setTermActive) {

	Result validateParam;
	if (!(validateParam = Utils.validateNotNull("year", year)).isSuccess()) {
	    return validateParam.toJAXRS();
	}
	if (!(validateParam = Utils.validateNotNull("quarter", quarter)).isSuccess()) {
	    return validateParam.toJAXRS();
	}

	Long startTime = System.currentTimeMillis();

	Result authCheckResult = Users.checkAuthenticationHelper(authUser, authToken, userAgent, 10, response);

	System.out.printf("%s, took %d milliseconds\n", "Done checking authentication",
		(System.currentTimeMillis() - startTime));
	startTime = System.currentTimeMillis();

	if (!authCheckResult.isSuccess()) {
	    return authCheckResult.toJAXRS();
	}

	Workbook workbook = new Workbook();
	Result insertResult;

	try {
	    String fileName = fileDetail.getFileName();
	    String fileExt = fileName.substring(fileName.lastIndexOf('.'));
	    if (fileExt.equalsIgnoreCase(".xls") || fileExt.equalsIgnoreCase(".xlsx")) {

		if (fileExt.equalsIgnoreCase(".xls")) {

		    // Create Workbook instance holding reference to
		    // .xls file
		    HSSFWorkbook inputWorkbook = new HSSFWorkbook(fileInputStream);

		    workbook.importFromWorkbook(inputWorkbook, true);

		    inputWorkbook.close();
		} else if (fileExt.equalsIgnoreCase(".xlsx")) {

		    // Create Workbook instance holding reference to
		    // .xlsx file
		    XSSFWorkbook inputWorkbook = new XSSFWorkbook(fileInputStream);

		    workbook.importFromWorkbook(inputWorkbook, true);

		    inputWorkbook.close();
		}
		fileInputStream.close();

	    } else {
		return new FailResult(400, "Unexpected file extension found: " + fileExt).toJAXRS();
	    }
	} catch (Exception e) {
	    return new FailResult(e).toJAXRS();
	}

	System.out.printf("%s, took %d milliseconds\n", "Done importing spreadsheet",
		(System.currentTimeMillis() - startTime));
	startTime = System.currentTimeMillis();

	try {
	    Sheet layoutSheet = workbook.getSheet("Layout");
	    Sheet dataSheet = workbook.getSheet("Data");
	    Sheet tablesSheet = workbook.getSheet("Tables");

	    // Build term/layout, and upload to DB
	    Term term = new Term(year, quarter, setTermActive);
	    term.setLayout_Section1(layoutSheet.getItem("Layout_Section1", "Value", Integer.class));
	    term.setLayout_Section2(layoutSheet.getItem("Layout_Section2", "Value", Integer.class));
	    term.setLayout_Section2_PathWidth(layoutSheet.getItem("Layout_Section2_PathWidth", "Value", Integer.class));
	    term.setLayout_Section2_Rows(layoutSheet.getItem("Layout_Section2_Rows", "Value", Integer.class));
	    term.setLayout_Section3(layoutSheet.getItem("Layout_Section3", "Value", Integer.class));
	    insertResult = term.insertIntoDB();
	    if (!insertResult.isSuccess()) {
		return insertResult.toJAXRS();
	    }

	    //
	    System.out.printf("%s, took %d milliseconds\n", "Done importing term/layout",
		    (System.currentTimeMillis() - startTime));
	    startTime = System.currentTimeMillis();

	    // Sort data to make sure companies are inserted in order.
	    dataSheet.sort(0, "StringAsc");

	    System.out.printf("%s, took %d milliseconds\n", "Done sorting data",
		    (System.currentTimeMillis() - startTime));
	    startTime = System.currentTimeMillis();

	    // Retrieve current categories, build map keyed on category names.
	    Result resp = CategoryMap.getCategories();
	    if (!resp.isSuccess()) {
		return resp.toJAXRS();
	    }
	    CategoryMap categoryMapById = resp.get("categoryMap", CategoryMap.class);
	    HashMap<String, Category> categoryMapByName = new HashMap<String, Category>();
	    ArrayList<String> allMajors = new ArrayList<String>();
	    ArrayList<String> allPositionTypes = new ArrayList<String>();
	    ArrayList<String> allWorkAuths = new ArrayList<String>();
	    for (Category c : categoryMapById.values()) {
		categoryMapByName.put(c.getName(), c);

		switch (c.getType()) {
		case "Major":
		    allMajors.add(c.getName());
		    break;
		case "Position Type":
		    allPositionTypes.add(c.getName());
		    break;
		case "Work Authorization":
		    allWorkAuths.add(c.getName());
		    break;
		default:
		    break;
		}
	    }

	    // Create map of companies keyed on name; will be used for table
	    // mappings.
	    HashMap<String, Company> companyMapByName = new HashMap<String, Company>();

	    // Insert data
	    for (int i = 0; i < dataSheet.getRows(); i++) {

		// Insert or update company as needed.
		String companyName = dataSheet.getItem(i, "companyName", String.class);
		String companyDescription = dataSheet.getItem(i, "companyDetailDescription", String.class);
		String companyWebsiteLink = dataSheet.getItem(i, "companyDetailWebsite-href", String.class);
		String companyAddress = dataSheet.getItem(i, "companyDetailAddress", String.class);

		if ("null".equalsIgnoreCase(companyDescription.trim())) {
		    companyDescription = null;
		}

		Company company = new Company(companyName, companyDescription, companyWebsiteLink, companyAddress);
		insertResult = company.insertIntoDB(term.getId());
		if (!insertResult.isSuccess()) {
		    return insertResult.toJAXRS();
		}
		companyMapByName.put(company.getName(), company);

		// Get all the different categories
		String collatedMajors = dataSheet.getItem(i, "companyMajor", String.class);
		String[] majors = collatedMajors == null ? new String[0] : collatedMajors.split(", ");
		if (majors.length == 0 || collatedMajors.matches(".*All .*")) {
		    majors = allMajors.toArray(majors);
		}

		String collatedWorkAuths = dataSheet.getItem(i, "companyWorkAuth", String.class);
		String[] workAuths = collatedWorkAuths == null ? new String[0] : collatedWorkAuths.split(", ");
		if (workAuths.length == 0 || collatedWorkAuths.matches(".*All .*")) {
		    workAuths = allWorkAuths.toArray(workAuths);
		}

		String collatedPositionTypes = dataSheet.getItem(i, "companyPositionType", String.class);
		String[] positionTypes = collatedPositionTypes == null ? new String[0]
			: collatedPositionTypes.split(", ");
		if (positionTypes.length == 0 || collatedPositionTypes.matches(".*All .*")) {
		    positionTypes = allPositionTypes.toArray(positionTypes);
		}

		String collatedIndustries = dataSheet.getItem(i, "companyDetailIndustry", String.class);
		String[] industries = collatedIndustries == null ? new String[0] : collatedIndustries.split(", ");

		// Insert categories and companyCategories into DB if needed.
		// Majors
		for (String major : majors) {

		    // Get or insert category
		    Category category = categoryMapByName.get(major);
		    if (category == null) {
			category = new Category(major, "Major");
			insertResult = category.insertIntoDB();
			if (!insertResult.isSuccess()) {
			    return insertResult.toJAXRS();
			}
			categoryMapByName.put(category.getName(), category);
		    }

		    // Insert or update companyCategory
		    CompanyCategory companycategory = new CompanyCategory(company.getId(), category.getId());
		    insertResult = companycategory.insertIntoDB();
		    if (!insertResult.isSuccess()) {
			return insertResult.toJAXRS();
		    }
		}

		// Position Types
		for (String positionType : positionTypes) {

		    // Get or insert category
		    Category category = categoryMapByName.get(positionType);
		    if (category == null) {
			category = new Category(positionType, "Position Type");
			insertResult = category.insertIntoDB();
			if (!insertResult.isSuccess()) {
			    return insertResult.toJAXRS();
			}
			categoryMapByName.put(category.getName(), category);
		    }

		    // Insert or update companyCategory
		    CompanyCategory companycategory = new CompanyCategory(company.getId(), category.getId());
		    insertResult = companycategory.insertIntoDB();
		    if (!insertResult.isSuccess()) {
			return insertResult.toJAXRS();
		    }
		}

		// Work Authorizations
		for (String workAuth : workAuths) {

		    // Get or insert category
		    Category category = categoryMapByName.get(workAuth);
		    if (category == null) {
			category = new Category(workAuth, "Work Authorization");
			insertResult = category.insertIntoDB();
			if (!insertResult.isSuccess()) {
			    return insertResult.toJAXRS();
			}
			categoryMapByName.put(category.getName(), category);
		    }

		    // Insert or update companyCategory
		    CompanyCategory companycategory = new CompanyCategory(company.getId(), category.getId());
		    insertResult = companycategory.insertIntoDB();
		    if (!insertResult.isSuccess()) {
			return insertResult.toJAXRS();
		    }
		}

		/*
		 * Disabled until front-end support available.
		 */
		// Industry Categories
		// for (String industry : industries) {
		//
		// // Get or insert category
		// Category category = categoryMapByName.get(industry);
		// if (category == null) {
		// category = new Category(industry, "Industry");
		// insertResult = category.insertIntoDB();
		// if (!insertResult.isSuccess()) {
		// return insertResult.toJAXRS();
		// }
		// categoryMapByName.put(category.getName(), category);
		// }
		//
		// // Insert or update companyCategory
		// CompanyCategory companycategory = new
		// CompanyCategory(company.getId(), category.getId());
		// insertResult = companycategory.insertIntoDB();
		// if (!insertResult.isSuccess()) {
		// return insertResult.toJAXRS();
		// }
		// }

		System.out.printf("%s %s, took %d milliseconds\n", "Done importing company", company.getName(),
			(System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
	    }

	    for (int i = 0; i < tablesSheet.getRows(); i++) {

		// Insert or update company as needed.
		Long tableId = tablesSheet.getItem(i, "Table", Long.class);
		String tableCompanyName = tablesSheet.getItem(i, "Company", String.class);
		Long tableCompanyId = null;
		if (tableCompanyName != null) {
		    tableCompanyId = companyMapByName.get(tableCompanyName).getId();
		}
		Integer tableSize = tablesSheet.getItem(i, "Size", Integer.class) == null ? 0
			: tablesSheet.getItem(i, "Size", Integer.class);

		TableMapping tableMapping = new TableMapping(tableId, tableCompanyId, tableSize);
		insertResult = tableMapping.insertIntoDB(term.getId());
		if (!insertResult.isSuccess()) {
		    return insertResult.toJAXRS();
		}

	    }

	    System.out.printf("%s, took %d milliseconds\n", "Done importing TableMappings",
		    (System.currentTimeMillis() - startTime));
	    startTime = System.currentTimeMillis();

	} catch (Exception e) {
	    e.printStackTrace();
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();
	}

	return new SuccessResult("Term data successfully uploaded.").toJAXRS();

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("category/all")
    public Response get_Categories() {
	try {

	    Result resp = CategoryMap.getCategories();
	    if (!resp.isSuccess()) {
		return resp.toJAXRS();
	    }
	    CategoryMap categoryMap = resp.get("categoryMap", CategoryMap.class);

	    SuccessResult response = new SuccessResult();
	    response.put("categoryMap", categoryMap);

	    return response.toJAXRS();
	} catch (Exception e) {
	    e.printStackTrace();
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();
	}
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{termId}/company/all")
    public Response get_Companies(@PathParam("termId") String termIdString) {

	Result getTermResult = getTerm(termIdString);
	if (!getTermResult.isSuccess()) {
	    return getTermResult.toJAXRS();
	}
	Term term = getTermResult.get("term", Term.class);

	try {

	    Result resp = CompanyMap.getCompanies(term.getId());
	    if (!resp.isSuccess()) {
		return resp.toJAXRS();
	    }
	    CompanyMap companyMap = resp.get("companyMap", CompanyMap.class);

	    SuccessResult response = new SuccessResult();
	    response.put("companyMap", companyMap);

	    return response.toJAXRS();
	} catch (Exception e) {
	    e.printStackTrace();
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();

	}
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{termId}/company_category/all")
    public Response get_CompanyCategories(@PathParam("termId") String termIdString) {

	Result getTermResult = getTerm(termIdString);
	if (!getTermResult.isSuccess()) {
	    return getTermResult.toJAXRS();
	}
	Term term = getTermResult.get("term", Term.class);

	try {

	    Result resp = CompanyCategoryMap.getCompanyCategories(term.getId());
	    if (!resp.isSuccess()) {
		return resp.toJAXRS();
	    }
	    CompanyCategoryMap companyCategoryMap = resp.get("companyCategoryMap", CompanyCategoryMap.class);

	    SuccessResult response = new SuccessResult();
	    response.put("companyCategoryMap", companyCategoryMap);

	    return response.toJAXRS();
	} catch (Exception e) {
	    e.printStackTrace();
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();

	}
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{termId}/term")
    public Response get_Term(@PathParam("termId") String termIdString) {

	return getTerm(termIdString).toJAXRS();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{termId}/term/active")
    public Response post_SetTermActive(@PathParam("termId") String termIdString, @QueryParam("active") Boolean active) {

	Result getTermResult = getTerm(termIdString);
	if (!getTermResult.isSuccess()) {
	    return getTermResult.toJAXRS();
	}
	Term term = getTermResult.get("term", Term.class);

	try {
	    return term.setActive(active).toJAXRS();
	} catch (ClassNotFoundException | SQLException e) {
	    e.printStackTrace();
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();
	}
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("all/term")
    public Response get_TermList(@DefaultValue("false") @QueryParam("showInactive") Boolean showInactive) {

	try {

	    Result resp = TermArray.getTermList(showInactive);
	    if (!resp.isSuccess()) {
		return resp.toJAXRS();
	    }
	    TermArray termList = resp.get("termList", TermArray.class);

	    SuccessResult response = new SuccessResult();
	    response.put("termList", termList);

	    return response.toJAXRS();
	} catch (Exception e) {
	    e.printStackTrace();
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();

	}
    }

    // @GET
    // @Produces(MediaType.APPLICATION_JSON)
    // @Path("latest/term")

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{termId}/table_mapping/all")
    public Response get_TableMappings(@PathParam("termId") String termIdString) {

	Result getTermResult = getTerm(termIdString);
	if (!getTermResult.isSuccess()) {
	    return getTermResult.toJAXRS();
	}
	Term term = getTermResult.get("term", Term.class);

	try {

	    Result resp = TableMappingArray.getTableMappings(term.getId());
	    if (!resp.isSuccess()) {
		return resp.toJAXRS();
	    }
	    TableMappingArray tableMappingList = resp.get("tableMappingList", TableMappingArray.class);

	    SuccessResult response = new SuccessResult();
	    response.put("tableMappingList", tableMappingList);

	    return response.toJAXRS();
	} catch (Exception e) {
	    e.printStackTrace();
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();

	}
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{termId}/table_mapping/all")
    public Response post_AllTableMappings(@PathParam("termId") String termIdString, String body) {

	Result getTermResult = getTerm(termIdString);
	if (!getTermResult.isSuccess()) {
	    return getTermResult.toJAXRS();
	}
	Term term = getTermResult.get("term", Term.class);

	try {

	    ObjectMapper mapper = new ObjectMapper();
	    TableMappingArray tableMappings = mapper.readValue(body, TableMappingArray.class);

	    for (TableMapping mapping : tableMappings) {

		Result insertResult = mapping.insertIntoDB(term.getId());
		if (!insertResult.isSuccess()) {
		    return insertResult.toJAXRS();
		}
	    }
	} catch (JsonMappingException e) {

	    return new FailResult(400, "Invalid JSON Body").toJAXRS();
	} catch (IOException | ClassNotFoundException | SQLException e) {
	    e.printStackTrace();
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();
	}

	return new SuccessResult().toJAXRS();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{termId}/statistics")
    public Response get_Statistics(@PathParam("termId") String termIdString) {

	Result getTermResult = getTerm(termIdString);
	if (!getTermResult.isSuccess()) {
	    return getTermResult.toJAXRS();
	}
	Term term = getTermResult.get("term", Term.class);

	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    stmt = conn.prepareCall("CALL Data_Get_Statistics(?);");
	    stmt.setLong(1, term.getId());

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs, 500)).isSuccess()) {
		return respObj.toJAXRS();
	    }

	    Integer companyCount = null, totalTableCount = null, usedTableCount = null;

	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		if (rs.next()) {

		    companyCount = rs.getInt("companyCount");
		    totalTableCount = rs.getInt("totalTableCount");
		    usedTableCount = rs.getInt("usedTableCount");
		}
	    }

	    SuccessResult response = new SuccessResult();
	    response.put("companyCount", companyCount);
	    response.put("totalTableCount", totalTableCount);
	    response.put("usedTableCount", usedTableCount);

	    return response.toJAXRS();
	} catch (Exception e) {
	    e.printStackTrace();
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();
	} finally {
	    if (rs != null) {
		try {
		    rs.close();
		} catch (SQLException e) {
		    e.printStackTrace();
		    ServletLog.logEvent(e);
		}
	    }
	    if (stmt != null) {
		try {
		    stmt.close();
		} catch (SQLException e) {
		    e.printStackTrace();
		    ServletLog.logEvent(e);
		}
	    }
	    if (conn != null) {
		try {
		    conn.close();
		} catch (SQLException e) {
		    e.printStackTrace();
		    ServletLog.logEvent(e);
		}
	    }
	}

    }

    private Result getTerm(String termIdString) {

	try {
	    Result getTermResult = termIdString.equalsIgnoreCase("Latest") ? get_Term_Latest()
		    : Term.getTerm(Long.valueOf(termIdString));
	    if (!getTermResult.isSuccess()) {
		return getTermResult;
	    }

	    SuccessResult response = new SuccessResult();
	    response.put("term", getTermResult.get("term"));

	    return response;

	} catch (NumberFormatException e) {
	    return new FailResult(400, "termId invalid");
	} catch (ClassNotFoundException | SQLException e) {
	    e.printStackTrace();
	    ServletLog.logEvent(e);

	    return new FailResult(e);
	}
    }

    private Result get_Term_Latest() {

	try {

	    Result resp = TermArray.getTermList(false);
	    if (!resp.isSuccess()) {
		return resp;
	    }
	    TermArray termList = resp.get("termList", TermArray.class);

	    SuccessResult response = new SuccessResult();
	    response.put("term", termList.get(termList.size() - 1));

	    return response;
	} catch (Exception e) {
	    e.printStackTrace();
	    ServletLog.logEvent(e);

	    return new FailResult(e);

	}
    }
}
