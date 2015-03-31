package servlets.users;

import java.io.IOException;
import java.util.HashMap;

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

public class UsersServlet extends HttpServlet {
    
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
    
    public UsersServlet() throws IOException {
    
        super();
        UsersRequestHandler.setupUserRequestHandler();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
        if (request.getParameter("method") != null && !request.getParameter("method").equalsIgnoreCase("login")
                && !request.getParameter("method").equalsIgnoreCase("registerUser")) {
            Response authResponse;
            if (!(authResponse = AuthManager.checkToken(request)).success) {
                
                response.getWriter().print(authResponse);
                return;
            }
        }
        
        response.setContentType("application/json");
        String method = request.getParameter("method") != null ? request.getParameter("method") : "null";
        
        Response responseObject;
        
        switch (method) {
            case "registerUser":
                responseObject = AuthManager.addUser(request);
                break;
            case "login":
                responseObject = AuthManager.authenticateUser(request);
                break;
            default:
                responseObject = new FailResponse("Invalid GET method supplied: " + method);
                break;
        }
        if (responseObject.cookie != null) {
            response.addCookie(responseObject.cookie);
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
