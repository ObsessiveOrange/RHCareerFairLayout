package servlets;

import javax.servlet.http.HttpServletRequest;

import common.Response;
import common.Response.SuccessResponse;

public class BaseRequestHandler {
    
    public static Response handleViewLogRequest(HttpServletRequest request) {
    
        Response response = new SuccessResponse();
        
        response.addToReturnData("Log", ServletLog.getLogJson());
        
        return response;
    }
}
