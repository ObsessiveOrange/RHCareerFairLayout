package servlets.users;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import misc.BCrypt;
import servlets.SystemVars;
import adt.Category;
import adt.Company;
import adt.DataVars;
import adt.ItemVars;
import adt.LayoutVars;

import com.google.gson.Gson;

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
        UsersRequestHandler.setupAdminRequestHandler();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
        if (request.getParameter("method") != null && !request.getParameter("method").equalsIgnoreCase("registerUser")) {
            if (!checkAuth(request)) {
                
                response.getWriter().print(SystemVars.failString("Invalid Username or Password supplied."));
                return;
            }
        }
        
        response.setContentType("application/json");
        String method = request.getParameter("method") != null ? request.getParameter("method") : "null";
        
        String responseString;
        
        switch (method) {
            case "registerUser":
                responseString = UsersRequestHandler.handleRegisterUserRequest(request);
                break;
            case "testDB":
                responseString = UsersRequestHandler.handleTestDbRequest(request);
                break;
            case "test":
                Map<String, String> env = System.getenv();
                Properties prop = System.getProperties();
                
                HashMap<String, Object> returnMap = new HashMap<String, Object>();
                
                returnMap.put("success", 0);
                returnMap.put("env", env);
                returnMap.put("prop", prop.entrySet());
                returnMap.put("timestamp", System.currentTimeMillis());
                
                responseString = new Gson().toJson(returnMap);
                break;
            // case "getUniqueID":
            // response.getWriter().print(RequestHandler.handleGetUniqueIDRequest(request));
            // break;
            // case "getCartItems":
            // response.getWriter().print(RequestHandler.handleGetCartItemsRequest(request));
            // break;
            // case "getAllCarts":
            // response.getWriter().print(RequestHandler.handleGetAllCartsRequest(request));
            // break;
            // case "getAllOrders":
            // response.getWriter().print(RequestHandler.handleGetAllOrdersRequest(request));
            // break;
            // case "null":
            default:
                responseString =
                        "{\"timestamp\":" + System.currentTimeMillis() + ", \"success\":0, \"error\":\"Invalid GET method supplied: " + method
                                + "\"}";
                break;
        }
        response.getWriter().print(responseString);
        System.out.println(responseString);
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
        response.setContentType("application/json");
        
        String method = request.getParameter("method") != null ? request.getParameter("method") : "null";
        
        String responseString;
        
        switch (method) {
            case "setSize":
                responseString = UsersRequestHandler.handleSetSizeRequest(request);
                break;
            case "forceRegenerationOfData":
                responseString = UsersRequestHandler.handleForceRegenerationOfData(request);
                break;
            // case "resetTestData":
            // response.getWriter().print(RequestHandler.resetTestData(request));
            // break;
            // case "submitOrder":
            // response.getWriter().print(RequestHandler.handleSubmitOrderRequest(request));
            // break;
            default:
                responseString =
                        "{\"timestamp\":" + System.currentTimeMillis() + ", \"success\":0, \"error\":\"Invalid POST method supplied: " + method
                                + "\"}";
                break;
        }
        response.getWriter().print(responseString);
        System.out.println(responseString);
    }
    
    private boolean checkAuth(HttpServletRequest request) {
    
        String user = request.getHeader("authUser");
        String pass = request.getHeader("authPass");
        
        Connection conn;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://" + SystemVars.getDbhost() + ":" + SystemVars.getDbport() + "/Users",
                    SystemVars.getDbusername(), SystemVars.getDbpassword());
            PreparedStatement prepStatement = conn.prepareStatement("SELECT hashedPw FROM users WHERE username = '" + user + "';");
            
            ResultSet result = prepStatement.executeQuery();
            result.next();
            
            return BCrypt.checkpw(pass, result.getString("hashedPw"));
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
