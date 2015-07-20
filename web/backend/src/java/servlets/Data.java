package servlets;

import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import adt.models.Term;
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
	    @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("param") String param) {

	SuccessResult s = new SuccessResult();

	System.out.println("1. " + fileDetail.getFileName());
	s.put("File name", fileDetail.getFileName());

	System.out.println("2. " + fileDetail.getSize());
	s.put("File size", fileDetail.getSize());

	System.out.println("3. " + fileDetail.getType());
	s.put("File type", fileDetail.getType());

	System.out.println("4. " + param);
	s.put("Param", param);

	return s.toJAXRS();
	// return new SuccessResponse();

    }

    // @POST
    // @Consumes("multipart/form-data")
    // @Produces(MediaType.APPLICATION_JSON)
    // @Path("all")
    // public Response post_AllData(@FormDataParam("file") InputStream
    // fileInputStream,
    // @FormDataParam("file") FormDataContentDisposition fileDetail,
    // @FormDataParam("term") String term) {
    //
    // // TODO: Authenticate.
    //
    // Workbook workbook = new Workbook();
    //
    // try {
    // String fileName = fileDetail.getFileName();
    // String fileExt = fileName.substring(fileName.lastIndexOf('.'));
    // if (fileExt.equalsIgnoreCase(".xls") ||
    // fileExt.equalsIgnoreCase(".xlsx")) {
    //
    // if (fileExt.equalsIgnoreCase(".xls")) {
    //
    // // Create Workbook instance holding reference to
    // // .xls file
    // HSSFWorkbook inputWorkbook = new HSSFWorkbook(fileInputStream);
    //
    // workbook.importFromWorkbook(inputWorkbook, true);
    //
    // inputWorkbook.close();
    // } else if (fileExt.equalsIgnoreCase(".xlsx")) {
    //
    // // Create Workbook instance holding reference to
    // // .xlsx file
    // XSSFWorkbook inputWorkbook = new XSSFWorkbook(fileInputStream);
    //
    // workbook.importFromWorkbook(inputWorkbook, true);
    //
    // inputWorkbook.close();
    // }
    // fileInputStream.close();
    //
    // } else {
    // return new FailResponse("Unexpected file extension found: " + fileExt);
    // }
    // } catch (Exception e) {
    // return new FailResponse(e);
    // }
    //
    // try {
    //
    // Response updateTermVarsResponse = DataManager.updateTermVars(term,
    // workbook.getSheet("Variables"));
    // if (!updateTermVarsResponse.isSuccess()) {
    // FailResponse failResponse = new FailResponse("Failed updating TermVars");
    // failResponse.put("updateTermVarsResponse", updateTermVarsResponse);
    // }
    // Response updateCategoriesAndCompaniesResponse =
    // DataManager.updateCategoriesAndCompanies(term,
    // workbook.getSheet("Categories"), workbook.getSheet("Companies"));
    // if (!updateCategoriesAndCompaniesResponse.isSuccess()) {
    // FailResponse failResponse = new FailResponse("Failed updating Categories
    // and Companies");
    // failResponse.put("updateCategoriesAndCompaniesResponse",
    // updateCategoriesAndCompaniesResponse);
    // }
    // Response updateTableMappingsResponse =
    // DataManager.updateTableMappings(term,
    // workbook.getSheet("TableMappings"),
    // updateCategoriesAndCompaniesResponse.get("companyList", List.class));
    // if (!updateTableMappingsResponse.isSuccess()) {
    // FailResponse failResponse = new FailResponse("Failed updating Table
    // Mappings");
    // failResponse.put("updateTableMappingsResponse",
    // updateTableMappingsResponse);
    // }
    //
    // } catch (Exception e) {
    // ServletLog.logEvent(e);
    //
    // return new FailResponse(e);
    // }
    //
    // Response s = new SuccessResponse("Term data successfully uploaded.");
    //
    // return s;
    //
    // }

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
    public Response get_Terms() {

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
	    if (!(respObj = Utils.checkResultSuccess(rs)).isSuccess()) {
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
