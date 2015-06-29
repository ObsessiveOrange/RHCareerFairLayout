/***Data Servlet**Serves data requests for public site;no authentication required.**@author Benedict Wong*/
package servlets.data;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.Response;
import common.Response.FailResponse;
import servlets.Servlet;

// Use Servlet 3.0 annotations
@WebServlet("/api/data")
public class DataServlet extends Servlet {

    /**
    *
    */
    private static final long serialVersionUID = -5982008108929904358L;

    public DataServlet() throws IOException {

	super();

	// loadDataFromFile();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

	// set return data type
	response.setContentType("application/json");

	// get supplied method, use "null" if none given.
	String method = request.getParameter("method") != null ? request.getParameter("method") : "null";

	// create response object
	Response responseObject;

	// select method based on the parameters sent.
	switch (method) {
	case "getSelectedTerm":
	    responseObject = DataRequestHandler.handleGetSelectedTermRequest();
	    break;
	case "getCategories":
	    responseObject = DataRequestHandler.handleGetCategoriesRequest();
	    break;
	case "getCompanies":
	    responseObject = DataRequestHandler.handleGetCompaniesRequest();
	    break;
	case "getLayout":
	    responseObject = DataRequestHandler.handleGetLayoutRequest();
	    break;
	case "getStatistics":
	    responseObject = DataRequestHandler.handleGetStatisticsRequest();
	    break;
	case "getData":
	    responseObject = DataRequestHandler.handleGetDataRequest();
	    break;
	// If invalid method header, return with an error;
	default:
	    responseObject = new FailResponse("Invalid GET method supplied to data servlet: " + method);
	    break;
	}

	// Set return content type and send data;
	sendResponse(response, responseObject);
    }
}
