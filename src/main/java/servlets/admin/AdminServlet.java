package servlets.admin;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import managers.AuthManager;
import servlets.ServletLog;
import servlets.ServletLog.LogEvent;
import adt.Category;
import adt.Company;
import adt.DataVars;
import adt.ItemVars;
import adt.LayoutVars;
import adt.Response;
import adt.Response.FailResponse;

@WebServlet("/api/users/admin")
@MultipartConfig(location = "/var/lib/openshift/5514734a4382ec499b000009/app-root/data")
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
            default:
                responseObject = new FailResponse("Invalid GET method supplied: " + method);
                break;
        }
        response.getWriter().print(responseObject);
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
        Response authResponse;
        if (!(authResponse = AuthManager.checkToken(request)).success) {
            
            response.getWriter().print(authResponse);
            return;
        }
        
        response.setContentType("application/json");
        
        Response fileUploadResponse = AdminRequestHandler.handleUploadRequest(request);
        if (fileUploadResponse.getFromReturnData("errorCode", Integer.class) != null
                && fileUploadResponse.getFromReturnData("errorCode", Integer.class) != -100) {
            response.getWriter().print(fileUploadResponse);
            return;
        }
        
        LogEvent event = new LogEvent();
        event.setDetail("Type", "Log");
        event.setDetail("id", 1);
        ServletLog.logEvent(event);
        response.getWriter().print(1);
        
        String method = request.getParameter("method") != null ? request.getParameter("method") : "null";
        
        event = new LogEvent();
        event.setDetail("Type", "Log");
        event.setDetail("id", 2);
        event.setDetail("method", method);
        ServletLog.logEvent(event);
        response.getWriter().print(2);
        
        Response responseObject;
        
        switch (method) {
            case "uploadData":
                event = new LogEvent();
                event.setDetail("Type", "Log");
                event.setDetail("id", 3);
                ServletLog.logEvent(event);
                response.getWriter().print(3);
                
                responseObject = AdminRequestHandler.handleUploadDataRequest(request, fileUploadResponse);
                break;
            case "newTerm":
                responseObject = AdminRequestHandler.handleNewTermRequest(request);
                break;
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
