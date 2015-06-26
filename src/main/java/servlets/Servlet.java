package servlets;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import adt.Response;

public class Servlet extends HttpServlet {
    
    /**
     * 
     */
    private static final long serialVersionUID = -6177231224521415031L;
    
    public static void sendResponse(HttpServletResponse connection, Response response) throws IOException {
    
        if (response.success && response.cookies != null) {
            for (Cookie c : response.cookies) {
                connection.addCookie(c);
            }
        }
        connection.getWriter().print(response);
    }
}
