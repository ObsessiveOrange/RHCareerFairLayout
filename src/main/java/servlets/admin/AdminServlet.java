// package servlets.admin;
//
// import java.io.IOException;
// import java.util.ArrayList;
//
// import javax.servlet.annotation.HttpConstraint;
// import javax.servlet.annotation.MultipartConfig;
// import javax.servlet.annotation.ServletSecurity;
// import javax.servlet.annotation.WebServlet;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
//
// import com.google.gson.Gson;
//
// import adt.TableMapping;
// import adt.TableMappingsWrapper;
// import adt.Workbook;
// import common.Response;
// import common.Response.FailResponse;
// import managers.AuthManager;
// import misc.RequestBody;
// import misc.Utils;
// import servlets.Servlet;
// import servlets.ServletUtils;
//
// @WebServlet("/api/users/admin")
// @MultipartConfig
// @ServletSecurity(value = @HttpConstraint(transportGuarantee =
// ServletSecurity.TransportGuarantee.CONFIDENTIAL) )
// public class AdminServlet extends Servlet {
//
// /**
// *
// */
// private static final long serialVersionUID = -5982008108929904358L;
//
// /** Getter & Setter Methods **/
//
// public AdminServlet() throws IOException {
//
// super();
// }
//
// @Override
// public void doGet(HttpServletRequest request, HttpServletResponse response)
// throws IOException {
//
// response.setContentType("application/json");
//
// Response authResponse;
// if (!(authResponse = AuthManager.checkToken(request)).isSuccess()) {
//
// ServletUtils.sendResponse(response, authResponse);
// return;
// }
//
// String method = request.getParameter("method") != null ?
// request.getParameter("method") : "null";
//
// Response responseObject;
//
// switch (method) {
// case "listTerms": {
// responseObject = AdminRequestHandler.listTerms();
// break;
// }
// default:
// responseObject = new FailResponse("Invalid GET method supplied: " + method);
// break;
// }
// sendResponse(response, responseObject);
// }
//
// @Override
// public void doPost(HttpServletRequest request, HttpServletResponse response)
// throws IOException {
//
// response.setContentType("application/json");
//
// Response authResponse;
// if (!(authResponse = AuthManager.checkToken(request)).isSuccess()) {
//
// ServletUtils.sendResponse(response, authResponse);
// return;
// }
//
// Response fileUploadResponse =
// AdminRequestHandler.handleUploadRequest(request);
// if (!fileUploadResponse.isSuccess()) {
// ServletUtils.sendResponse(response, fileUploadResponse);
// return;
// }
//
// String method = request.getParameter("method") != null ?
// request.getParameter("method") : "null";
//
// Response responseObject;
// RequestBody body = new RequestBody(request);
//
// switch (method) {
// case "uploadData": {
//
// String year = null, quarter = null;
//
// if (body.getString("year", null, null).success && body.getString("quarter",
// null, null).success) {
//
// year = body.getString("year", null, null).result;
// quarter = body.getString("quarter", null, null).result;
// } else {
// responseObject = new FailResponse("Missing or invalid POST body
// parameters.");
// }
//
// Workbook uploadedWorkbook = fileUploadResponse.get("uploadedWorkbook",
// Workbook.class);
//
// responseObject = AdminRequestHandler.uploadData(year, quarter,
// uploadedWorkbook);
// break;
// }
// case "newTerm": {
//
// String year = request.getHeader("year");
// String quarter = request.getHeader("quarter");
//
// Response checkResponse;
// if (!(checkResponse = Utils.validateStrings(null, null, request, "year",
// "quarter")).isSuccess()
// || !Utils.validateTerm(year, quarter).isSuccess()) {
// responseObject = checkResponse;
// break;
// }
//
// responseObject = AdminRequestHandler.createNewTerm(year, quarter);
// break;
// }
// case "setTerm": {
//
// String year = request.getHeader("year");
// String quarter = request.getHeader("quarter");
//
// Response checkResponse;
// if (!(checkResponse = Utils.validateStrings(null, null, request, "year",
// "quarter")).isSuccess()
// || !Utils.validateTerm(year, quarter).isSuccess()) {
// responseObject = checkResponse;
// break;
// }
// responseObject = AdminRequestHandler.setTerm(year, quarter);
// break;
// }
// case "updateTableMappings": {
//
// String bodyData = ServletUtils.getBodyData(request);
//
// ArrayList<TableMapping> mappings = new Gson().fromJson(bodyData,
// TableMappingsWrapper.class).updatedMappings;
//
// if (!Utils.validateObjects(mappings).isSuccess()) {
//
// ServletUtils.sendResponse(response, new FailResponse("Mapping null, check
// input"));
// }
//
// responseObject = AdminRequestHandler.updateTableMappingsHandler(mappings);
// break;
// }
// default: {
// responseObject = new FailResponse("Invalid POST method supplied: " + method);
// break;
// }
// }
// sendResponse(response, responseObject);
// }
// }
