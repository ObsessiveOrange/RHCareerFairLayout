/**
 * Data Servlet
 * 
 * Serves data requests for public site; no authentication required.
 * 
 * @author Benedict Wong
 */
package servlets.data;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import servlets.ServletUtils;
import adt.Response;
import adt.Response.FailResponse;

//Use Servlet 3.0 annotations
@WebServlet("/api/data")
public class DataServlet extends HttpServlet {
    
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
                responseObject = DataRequestHandler.handleGetSelectedTermRequest(request);
                break;
            case "getCategories":
                responseObject = DataRequestHandler.handleGetCategoriesRequest(request);
                break;
            case "getCompanies":
                responseObject = DataRequestHandler.handleGetCompaniesRequest(request);
                break;
            case "getLayout":
                responseObject = DataRequestHandler.handleGetLayoutRequest(request);
                break;
            case "getStatistics":
                responseObject = DataRequestHandler.handleGetStatisticsRequest(request);
                break;
            case "getData":
                responseObject = DataRequestHandler.handleGetDataRequest(request);
                break;
            // If invalid method header, return with an error;
            default:
                responseObject = new FailResponse("Invalid GET method supplied to data servlet: " + method);
                break;
        }
        
        // Set return content type and send data;
        ServletUtils.sendResponse(response, responseObject);
    }
}
