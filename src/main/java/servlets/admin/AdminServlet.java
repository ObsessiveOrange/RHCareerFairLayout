package servlets.admin;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import managers.AuthManager;
import adt.Category;
import adt.Company;
import adt.DataVars;
import adt.ItemVars;
import adt.LayoutVars;
import adt.Response;
import adt.Response.FailResponse;

@WebServlet(urlPatterns = "/api/users/*")
@ServletSecurity(value = @HttpConstraint(transportGuarantee = TransportGuarantee.CONFIDENTIAL))
public class AdminServlet extends HttpServlet {
    
    /**
     * 
     */
    private static final long                serialVersionUID = -5982008108929904358L;
    
    public static DataVars                   dataVars;
    public static LayoutVars                 layoutVars;
    public static ItemVars                   systemVars;
    public static HashMap<Integer, Category> categoryMap      = new HashMap<Integer, Category>();
    public static HashMap<Integer, Company>  entryMap         = new HashMap<Integer, Company>();
    
    public static boolean                    isTest           = true;
    
    /** Getter & Setter Methods **/
    
    public AdminServlet() throws IOException {
    
        super();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
        Response authResponse;
        if (!(authResponse = AuthManager.checkToken(request)).success) {
            
            response.getWriter().print(authResponse);
            return;
        }
        
        response.setContentType("application/json");
        String method = request.getParameter("method") != null ? request.getParameter("method") : "null";
        
        Response responseObject;
        
        switch (method) {
            case "newTerm":
                responseObject = AdminRequestHandler.handleNewTermRequest(request);
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
            case "setSize":
                responseObject = AdminRequestHandler.handleSetSizeRequest(request);
                break;
            default:
                responseObject = new FailResponse("Invalid POST method supplied: " + method);
                break;
        }
        response.getWriter().print(responseObject);
    }
}
