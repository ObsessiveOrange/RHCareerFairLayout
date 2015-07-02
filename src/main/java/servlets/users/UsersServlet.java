// package servlets.users;
//
// import java.io.IOException;
//
// import javax.servlet.annotation.HttpConstraint;
// import javax.servlet.annotation.ServletSecurity;
// import javax.servlet.annotation.WebServlet;
// import javax.servlet.http.HttpServlet;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
//
// import common.Response;
// import common.Response.FailResponse;
// import common.Response.SuccessResponse;
// import managers.AuthManager;
// import servlets.ServletUtils;
//
// @WebServlet("/api/users")
// @ServletSecurity(value = @HttpConstraint(transportGuarantee =
// ServletSecurity.TransportGuarantee.CONFIDENTIAL) )
// public class UsersServlet extends HttpServlet {
//
// /**
// *
// */
// private static final long serialVersionUID = -2790596508036351419L;
//
// /** Getter & Setter Methods **/
//
// public UsersServlet() throws IOException {
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
// // check if the user has already been authenticated.
// if (!(authResponse = AuthManager.checkToken(request)).isSuccess()) {
// // if fails authentication check, return the error.
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
// default:
// responseObject = new FailResponse("Invalid GET method supplied: " + method);
// break;
// }
// ServletUtils.sendResponse(response, responseObject);
// }
//
// @Override
// public void doPost(HttpServletRequest request, HttpServletResponse response)
// throws IOException {
//
// response.setContentType("application/json");
//
// if (request.getParameter("method") != null &&
// !request.getParameter("method").equalsIgnoreCase("login")
// && !request.getParameter("method").equalsIgnoreCase("registerUser")) {
// Response authResponse;
// if (!(authResponse = AuthManager.checkToken(request)).isSuccess()) {
//
// ServletUtils.sendResponse(response, authResponse);
// return;
// }
// }
//
// String method = request.getParameter("method") != null ?
// request.getParameter("method") : "null";
//
// Response responseObject;
//
// switch (method) {
// case "checkAuthentication":
// responseObject = new SuccessResponse("Authenticated");
// break;
// case "login":
// responseObject = AuthManager.authenticateUser(request);
// break;
// case "logout":
// responseObject = AuthManager.logoutUser(request);
// break;
// case "registerUser":
// responseObject = AuthManager.addUser(request);
// break;
// default:
// responseObject = new FailResponse("Invalid POST method supplied: " + method);
// break;
// }
// ServletUtils.sendResponse(response, responseObject);
// }
// }
