package servlets;

import java.io.IOException;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.Response;
import common.Response.FailResponse;

@WebServlet("/api")
@MultipartConfig(location = "/var/lib/openshift/5514734a4382ec499b000009/app-root/data")
public class BaseServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -5982008108929904358L;

    /** Getter & Setter Methods **/

    public BaseServlet() throws IOException {

	super();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

	response.setContentType("application/json");
	String method = request.getParameter("method") != null ? request.getParameter("method") : "null";

	Response responseObject;

	switch (method) {
	case "viewLog":
	    responseObject = BaseRequestHandler.handleViewLogRequest(request);
	    break;
	default:
	    responseObject = new FailResponse("Invalid GET method supplied: " + method);
	    break;
	}
	response.getWriter().print(responseObject);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

	response.setContentType("application/json");
	String method = request.getParameter("method") != null ? request.getParameter("method") : "null";

	Response responseObject;

	switch (method) {
	default:
	    responseObject = new FailResponse("Invalid POST method supplied: " + method);
	    break;
	}
	response.getWriter().print(responseObject);
    }
}
