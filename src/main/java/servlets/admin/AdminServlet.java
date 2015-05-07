package servlets.admin;

import java.io.IOException;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import managers.AuthManager;
import misc.Utils;
import adt.Response;
import adt.Response.FailResponse;
import adt.Workbook;

@WebServlet("/api/users/admin")
@MultipartConfig
@ServletSecurity(value = @HttpConstraint(transportGuarantee = ServletSecurity.TransportGuarantee.CONFIDENTIAL))
public class AdminServlet extends HttpServlet {
    
    /**
     * 
     */
    private static final long serialVersionUID = -5982008108929904358L;
    
    /** Getter & Setter Methods **/
    
    public AdminServlet() throws IOException {
    
        super();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
        response.setContentType("application/json");
        
        Response authResponse;
        if (!(authResponse = AuthManager.checkToken(request)).success) {
            
            response.getWriter().print(authResponse);
            return;
        }
        
        String method = request.getParameter("method") != null ? request.getParameter("method") : "null";
        
        Response responseObject;
        
        switch (method) {
            case "listTerms": {
                responseObject = AdminRequestHandler.listTerms();
                break;
            }
            default:
                responseObject = new FailResponse("Invalid GET method supplied: " + method);
                break;
        }
        response.getWriter().print(responseObject);
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
        response.setContentType("application/json");
        
        Response authResponse;
        if (!(authResponse = AuthManager.checkToken(request)).success) {
            
            response.getWriter().print(authResponse);
            return;
        }
        
        Response fileUploadResponse = AdminRequestHandler.handleUploadRequest(request);
        if (fileUploadResponse.getFromReturnData("errorCode", Integer.class) != null
                && fileUploadResponse.getFromReturnData("errorCode", Integer.class) != -100) {
            response.getWriter().print(fileUploadResponse);
            return;
        }
        
        String method = request.getParameter("method") != null ? request.getParameter("method") : "null";
        
        Response responseObject;
        
        switch (method) {
            case "uploadData": {
                String year = request.getHeader("year");
                String quarter = request.getHeader("quarter");
                Workbook uploadedWorkbook = fileUploadResponse.getFromReturnData("uploadedWorkbook", Workbook.class);
                
                if (!Utils.validateArgs(year, quarter, uploadedWorkbook) || !Utils.validateTerm(year, quarter)) {
                    
                    responseObject = new FailResponse("Invalid parameters provided");
                    break;
                }
                
                responseObject = AdminRequestHandler.uploadData(year, quarter, uploadedWorkbook);
                break;
            }
            case "newTerm": {
                
                String year = request.getHeader("year");
                String quarter = request.getHeader("quarter");
                
                if (!Utils.validateArgs(year, quarter) || !Utils.validateTerm(year, quarter)) {
                    
                    responseObject = new FailResponse("Invalid parameters provided");
                    break;
                }
                
                responseObject = AdminRequestHandler.createNewTerm(year, quarter);
                break;
            }
            case "setTerm": {
                
                String year = request.getHeader("year");
                String quarter = request.getHeader("quarter");
                
                if (!Utils.validateArgs(year, quarter) || !Utils.validateTerm(year, quarter)) {
                    
                    responseObject = new FailResponse("Invalid parameters provided");
                    break;
                }
                responseObject = AdminRequestHandler.setTerm(year, quarter);
                break;
            }
            default: {
                responseObject = new FailResponse("Invalid POST method supplied: " + method);
                break;
            }
        }
        response.getWriter().print(responseObject);
    }
}
