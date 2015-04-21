package servlets.users;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import managers.AuthManager;
import adt.Response;
import adt.Response.FailResponse;

@WebServlet("/api/users")
public class UsersServlet extends HttpServlet {
    
    /**
     * 
     */
    private static final long serialVersionUID = -2790596508036351419L;
    
    /** Getter & Setter Methods **/
    
    public UsersServlet() throws IOException {
    
        super();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
        Response authResponse;
        // check if the user has already been authenticated.
        if (!(authResponse = AuthManager.checkToken(request)).success) {
            // if fails authentication check, return the error.
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
        if (responseObject.cookie != null) {
            response.addCookie(responseObject.cookie);
        }
        response.getWriter().print(responseObject);
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
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
            case "login":
                responseObject = AuthManager.authenticateUser(request);
                break;
            case "registerUser":
                responseObject = AuthManager.addUser(request);
                break;
            default:
                responseObject = new FailResponse("Invalid POST method supplied: " + method);
                break;
        }
        if (responseObject.cookie != null) {
            response.addCookie(responseObject.cookie);
            responseObject.addToReturnData("Test", "Test");
        }
        response.getWriter().print(responseObject);
    }
}
