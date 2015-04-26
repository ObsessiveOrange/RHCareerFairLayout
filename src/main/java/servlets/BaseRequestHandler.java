package servlets;

import javax.servlet.http.HttpServletRequest;

import adt.Response;
import adt.Response.SuccessResponse;

public class BaseRequestHandler {
    
    public static Response handleViewLogRequest(HttpServletRequest request) {
    
        Response response = new SuccessResponse();
        
        response.addToReturnData("Log", ServletLog.getLogJson());
        
        return response;
    }
}
