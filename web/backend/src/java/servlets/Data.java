package servlets;

import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

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
    @Path("all/latest")
    public Response get_AllData_Latest() {
	try {
	    Result resp = TermArray.getTermList();
	    if (!resp.isSuccess()) {
		return resp.toJAXRS();
	    }
	    TermArray termList = resp.get("termList", TermArray.class);

	    if (termList.isEmpty()) {
		return new FailResult(400, "No terms found").toJAXRS();
	    }
	    Term latestTerm = termList.get(0);

	    Integer year = latestTerm.getYear();
	    String quarter = latestTerm.getQuarter();
	    return get_AllData(year, quarter);

	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();
	}
    }

    @GET
    // @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("all")
    public Response get_AllData(@QueryParam("year") Integer year, @QueryParam("quarter") String quarter) {

	Result validateParam;
	if (!(validateParam = Utils.validateNotNull("year", year)).isSuccess()) {
	    return validateParam.toJAXRS();
	}
	if (!(validateParam = Utils.validateNotNull("quarter", quarter)).isSuccess()) {
	    return validateParam.toJAXRS();
	}

	try {
	    SuccessResult response = new SuccessResult();

	    Result r;
	    r = Term.getTerm(year, quarter);
	    if (!r.isSuccess()) {
		return r.toJAXRS();
	    } else {
		response.put("term", r.get("term"));
	    }

	    r = CategoryMap.getCategories();
	    if (!r.isSuccess()) {
		return r.toJAXRS();
	    } else {
		response.put("categoryMap", r.get("categoryMap"));
	    }

	    r = CompanyCategoryMap.getCompanyCategories(year, quarter);
	    if (!r.isSuccess()) {
		return r.toJAXRS();
	    } else {
		response.put("companyCategoryMap", r.get("companyCategoryMap"));
	    }

	    r = CompanyMap.getCompanies(year, quarter);
	    if (!r.isSuccess()) {
		return r.toJAXRS();
	    } else {
		response.put("companyMap", r.get("companyMap"));
	    }

	    r = TableMappingArray.getTableMappings(year, quarter);
	    if (!r.isSuccess()) {
		return r.toJAXRS();
	    } else {
		response.put("tableMappingList", r.get("tableMappingList"));
	    }

	    return response.toJAXRS();

	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();
	}
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("test")
    public Response test(@FormDataParam("file") InputStream fileInputStream,
	    @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("year") Integer year,
	    @FormDataParam("quarter") String quarter) {

	SuccessResult s = new SuccessResult();

	System.out.println("1. " + fileDetail.getFileName());
	s.put("File name", fileDetail.getFileName());

	System.out.println("2. " + fileDetail.getSize());
	s.put("File size", fileDetail.getSize());

	System.out.println("3. " + fileDetail.getType());
	s.put("File type", fileDetail.getType());

	System.out.println("4. " + year);
	s.put("year", year);

	System.out.println("5. " + quarter);
	s.put("quarter", quarter);

	return s.toJAXRS();
	// return new SuccessResult();

    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("all")
    public Result post_AllData(@CookieParam("authUser") String authUser, @CookieParam("authToken") String authToken,
	    @HeaderParam("User-Agent") String userAgent, @FormDataParam("data") InputStream fileInputStream,
	    @FormDataParam("data") FormDataContentDisposition fileDetail, @FormDataParam("year") Integer year,
	    @FormDataParam("quarter") String quarter) {

	Long startTime = System.currentTimeMillis();

	Result authCheckResult = Users.checkAuthenticationHelper(authUser, authToken, userAgent, 10, response);

	System.out.printf("%s, took %d milliseconds\n", "Done checking authentication",
		(System.currentTimeMillis() - startTime));
	startTime = System.currentTimeMillis();

	if (!authCheckResult.isSuccess()) {
	    return authCheckResult;
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
		return new FailResult(400, "Unexpected file extension found: " + fileExt);
	    }
	} catch (Exception e) {
	    return new FailResult(e);
	}

	System.out.printf("%s, took %d milliseconds\n", "Done importing spreadsheet",
		(System.currentTimeMillis() - startTime));
	startTime = System.currentTimeMillis();

	try {
	    Sheet layoutSheet = workbook.getSheet("Layout");
	    Sheet dataSheet = workbook.getSheet("Data");

	    Term term = new Term(year, quarter);
	    term.setLayout_Section1(layoutSheet.getItem("Layout_Section1", "Value", Integer.class));
	    term.setLayout_Section2(layoutSheet.getItem("Layout_Section2", "Value", Integer.class));
	    term.setLayout_Section2_PathWidth(layoutSheet.getItem("Layout_Section2_PathWidth", "Value", Integer.class));
	    term.setLayout_Section2_Rows(layoutSheet.getItem("Layout_Section2_Rows", "Value", Integer.class));
	    term.setLayout_Section3(layoutSheet.getItem("Layout_Section3", "Value", Integer.class));
	    term.insertIntoDB();

	    System.out.printf("%s, took %d milliseconds\n", "Done importing layout",
		    (System.currentTimeMillis() - startTime));
	    startTime = System.currentTimeMillis();

	    dataSheet.sort(0, "StringAsc");

	    System.out.printf("%s, took %d milliseconds\n", "Done sorting data",
		    (System.currentTimeMillis() - startTime));
	    startTime = System.currentTimeMillis();

	    // Retrieve current categories
	    Result resp = CategoryMap.getCategories();
	    if (!resp.isSuccess()) {
		return resp;
	    }
	    CategoryMap categoryMapById = resp.get("categoryMap", CategoryMap.class);
	    HashMap<String, Category> categoryMap = new HashMap<String, Category>();
	    for (Category c : categoryMapById.values()) {
		categoryMap.put(c.getName(), c);
	    }

	    // Insert data
	    for (int i = 0; i < dataSheet.getRows(); i++) {

		// Insert or update company as needed.
		String companyName = dataSheet.getItem(i, "companyName", String.class);
		String companyDescription = dataSheet.getItem(i, "companyDetailDescription", String.class);
		String companyWebsiteLink = dataSheet.getItem(i, "companyDetailWebsite-href", String.class);
		String companyAddress = dataSheet.getItem(i, "companyDetailAddress", String.class);

		Company company = new Company(companyName, companyDescription, companyWebsiteLink, companyAddress);
		insertResult = company.insertIntoDB(term.getId());
		if (!insertResult.isSuccess()) {
		    return insertResult;
		}

		// Get all the different categories
		String collatedMajors = dataSheet.getItem(i, "companyMajor", String.class);
		String[] majors = (collatedMajors == null ? "" : collatedMajors).split(", ");

		String collatedWorkAuths = dataSheet.getItem(i, "companyWorkAuth", String.class);
		String[] workAuths = (collatedWorkAuths == null ? "" : collatedWorkAuths).split(", ");

		String collatedPositionTypes = dataSheet.getItem(i, "companyPositionType", String.class);
		String[] positionTypes = (collatedPositionTypes == null ? "" : collatedPositionTypes).split(", ");

		String collatedIndustries = dataSheet.getItem(i, "companyDetailIndustry", String.class);
		String[] industries = (collatedIndustries == null ? "" : collatedIndustries).split(", ");

		// Insert categories and companyCategories into DB if needed.
		// Majors
		for (String major : majors) {

		    // Get or insert category
		    Category category = categoryMap.get(major);
		    if (category == null) {
			category = new Category(major, "Major");
			insertResult = category.insertIntoDB();
			if (!insertResult.isSuccess()) {
			    return insertResult;
			}
			categoryMap.put(category.getName(), category);
		    }

		    // Insert or update companyCategory
		    CompanyCategory companycategory = new CompanyCategory(company.getId(), category.getId());
		    insertResult = companycategory.insertIntoDB();
		    if (!insertResult.isSuccess()) {
			return insertResult;
		    }
		}

		// Position Types
		for (String positionType : positionTypes) {

		    // Get or insert category
		    Category category = categoryMap.get(positionType);
		    if (category == null) {
			category = new Category(positionType, "Position Type");
			insertResult = category.insertIntoDB();
			if (!insertResult.isSuccess()) {
			    return insertResult;
			}
			categoryMap.put(category.getName(), category);
		    }

		    // Insert or update companyCategory
		    CompanyCategory companycategory = new CompanyCategory(company.getId(), category.getId());
		    insertResult = companycategory.insertIntoDB();
		    if (!insertResult.isSuccess()) {
			return insertResult;
		    }
		}

		// Work Authorizations
		for (String workAuth : workAuths) {

		    // Get or insert category
		    Category category = categoryMap.get(workAuth);
		    if (category == null) {
			category = new Category(workAuth, "Work Authorization");
			insertResult = category.insertIntoDB();
			if (!insertResult.isSuccess()) {
			    return insertResult;
			}
			categoryMap.put(category.getName(), category);
		    }

		    // Insert or update companyCategory
		    CompanyCategory companycategory = new CompanyCategory(company.getId(), category.getId());
		    insertResult = companycategory.insertIntoDB();
		    if (!insertResult.isSuccess()) {
			return insertResult;
		    }
		}

		// Industry Categories
		for (String industry : industries) {

		    // Get or insert category
		    Category category = categoryMap.get(industry);
		    if (category == null) {
			category = new Category(industry, "Industry");
			insertResult = category.insertIntoDB();
			if (!insertResult.isSuccess()) {
			    return insertResult;
			}
			categoryMap.put(category.getName(), category);
		    }

		    // Insert or update companyCategory
		    CompanyCategory companycategory = new CompanyCategory(company.getId(), category.getId());
		    insertResult = companycategory.insertIntoDB();
		    if (!insertResult.isSuccess()) {
			return insertResult;
		    }
		}

		System.out.printf("%s %s, took %d milliseconds\n", "Done importing company", company.getName(),
			(System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
	    }

	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResult(e);
	}

	return new SuccessResult("Term data successfully uploaded.");

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
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();
	}
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("company/all")
    public Response get_Companies(@QueryParam("year") Integer year, @QueryParam("quarter") String quarter) {

	Result validateParam;
	if (!(validateParam = Utils.validateNotNull("year", year)).isSuccess()) {
	    return validateParam.toJAXRS();
	}
	if (!(validateParam = Utils.validateNotNull("quarter", quarter)).isSuccess()) {
	    return validateParam.toJAXRS();
	}

	try {

	    Result resp = CompanyMap.getCompanies(year, quarter);
	    if (!resp.isSuccess()) {
		return resp.toJAXRS();
	    }
	    CompanyMap companyMap = resp.get("companyMap", CompanyMap.class);

	    SuccessResult response = new SuccessResult();
	    response.put("companyMap", companyMap);

	    return response.toJAXRS();
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();

	}
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("company_category/all")
    public Response get_CompanyCategories(@QueryParam("year") Integer year, @QueryParam("quarter") String quarter) {

	Result validateParam;
	if (!(validateParam = Utils.validateNotNull("year", year)).isSuccess()) {
	    return validateParam.toJAXRS();
	}
	if (!(validateParam = Utils.validateNotNull("quarter", quarter)).isSuccess()) {
	    return validateParam.toJAXRS();
	}

	try {

	    Result resp = CompanyCategoryMap.getCompanyCategories(year, quarter);
	    if (!resp.isSuccess()) {
		return resp.toJAXRS();
	    }
	    CompanyCategoryMap companyCategoryMap = resp.get("companyCategoryMap", CompanyCategoryMap.class);

	    SuccessResult response = new SuccessResult();
	    response.put("companyCategoryMap", companyCategoryMap);

	    return response.toJAXRS();
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();

	}
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("term")
    public Response get_Term(@QueryParam("year") Integer year, @QueryParam("quarter") String quarter) {

	Result validateParam;
	if (!(validateParam = Utils.validateNotNull("year", year)).isSuccess()) {
	    return validateParam.toJAXRS();
	}
	if (!(validateParam = Utils.validateNotNull("quarter", quarter)).isSuccess()) {
	    return validateParam.toJAXRS();
	}

	try {

	    Result resp = Term.getTerm(year, quarter);
	    if (!resp.isSuccess()) {
		return resp.toJAXRS();
	    }
	    Term term = resp.get("term", Term.class);

	    SuccessResult response = new SuccessResult();
	    response.put("term", term);

	    return response.toJAXRS();
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();

	}
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("term/all")
    public Response get_TermList() {

	try {

	    Result resp = TermArray.getTermList();
	    if (!resp.isSuccess()) {
		return resp.toJAXRS();
	    }
	    TermArray termList = resp.get("termList", TermArray.class);

	    SuccessResult response = new SuccessResult();
	    response.put("termList", termList);

	    return response.toJAXRS();
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();

	}
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("term/latest")
    public Response get_Term_Latest() {

	try {

	    Result resp = TermArray.getTermList();
	    if (!resp.isSuccess()) {
		return resp.toJAXRS();
	    }
	    TermArray termList = resp.get("termList", TermArray.class);

	    SuccessResult response = new SuccessResult();
	    response.put("term", termList.get(0));

	    return response.toJAXRS();
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();

	}
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("table_mapping/all")
    public Response get_TableMappings(@QueryParam("year") Integer year, @QueryParam("quarter") String quarter) {

	Result validateParam;
	if (!(validateParam = Utils.validateNotNull("year", year)).isSuccess()) {
	    return validateParam.toJAXRS();
	}
	if (!(validateParam = Utils.validateNotNull("quarter", quarter)).isSuccess()) {
	    return validateParam.toJAXRS();
	}

	try {

	    Result resp = TableMappingArray.getTableMappings(year, quarter);
	    if (!resp.isSuccess()) {
		return resp.toJAXRS();
	    }
	    TableMappingArray tableMappingList = resp.get("tableMappingList", TableMappingArray.class);

	    SuccessResult response = new SuccessResult();
	    response.put("tableMappingList", tableMappingList);

	    return response.toJAXRS();
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();

	}
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("table_mapping/all")
    public Response post_AllTableMappings(@QueryParam("year") Integer year, @QueryParam("quarter") String quarter,
	    String body) throws ClassNotFoundException, SQLException {

	ObjectMapper mapper = new ObjectMapper();

	try {
	    TableMappingArray tableMappings = mapper.readValue(body, TableMappingArray.class);

	    for (TableMapping mapping : tableMappings) {
		Result getTermResult = Term.getTerm(year, quarter);
		if (!getTermResult.isSuccess()) {
		    return getTermResult.toJAXRS();
		}
		Term term = (Term) getTermResult.get("term");

		mapping.insertIntoDB(term.getId());
	    }
	} catch (IOException e) {
	    ServletLog.logEvent(e);

	    return new FailResult(400, "Invalid JSON Body").toJAXRS();
	}

	return new SuccessResult(body).toJAXRS();
	// Result validateParam;
	// if (!(validateParam = Utils.validateNotNull("year",
	// year)).isSuccess()) {
	// return validateParam.toJAXRS();
	// }
	// if (!(validateParam = Utils.validateNotNull("quarter",
	// quarter)).isSuccess()) {
	// return validateParam.toJAXRS();
	// }
	//
	// try {
	//
	// Result resp = TableMappingArray.getTableMappings(year, quarter);
	// if (!resp.isSuccess()) {
	// return resp.toJAXRS();
	// }
	// TableMappingArray tableMappingList = resp.get("tableMappingList",
	// TableMappingArray.class);
	//
	// SuccessResult response = new SuccessResult();
	// response.put("tableMappingList", tableMappingList);
	//
	// return response.toJAXRS();
	// } catch (Exception e) {
	// ServletLog.logEvent(e);
	//
	// return new FailResult(e).toJAXRS();
	//
	// }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("statistics")
    public Response get_Statistics(@QueryParam("year") Integer year, @QueryParam("quarter") String quarter) {

	Result validateParam;
	if (!(validateParam = Utils.validateNotNull("year", year)).isSuccess()) {
	    return validateParam.toJAXRS();
	}
	if (!(validateParam = Utils.validateNotNull("quarter", quarter)).isSuccess()) {
	    return validateParam.toJAXRS();
	}

	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    stmt = conn.prepareCall("CALL Data_Get_Statistics(?, ?);");
	    stmt.setInt(1, year);
	    stmt.setString(2, quarter);

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
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();
	} finally {
	    if (rs != null) {
		try {
		    rs.close();
		} catch (SQLException e) {
		    ServletLog.logEvent(e);
		}
	    }
	    if (stmt != null) {
		try {
		    stmt.close();
		} catch (SQLException e) {
		    ServletLog.logEvent(e);
		}
	    }
	    if (conn != null) {
		try {
		    conn.close();
		} catch (SQLException e) {
		    ServletLog.logEvent(e);
		}
	    }
	}

    }
}
