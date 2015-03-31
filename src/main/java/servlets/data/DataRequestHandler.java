package servlets.data;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import adt.Category;
import adt.Company;
import adt.LayoutVars;
import adt.Response;
import adt.Response.SuccessResponse;

public class DataRequestHandler {
    
    public static Response handleGetDataRequest(HttpServletRequest request) throws IOException {
    
        HashMap<Integer, Category> categoryMap = DataServlet.categoryMap;
        HashMap<Integer, Company> entryMap = DataServlet.entryMap;
        LayoutVars layout = DataServlet.layoutVars;
        
        SuccessResponse response = new SuccessResponse();
        response.addToReturnData("title", "Career Fair " + DataServlet.dataVars.getQuarter() + " "
                + DataServlet.dataVars.getYear());
        response.addToReturnData("categories", categoryMap);
        response.addToReturnData("entries", entryMap);
        response.addToReturnData("layout", layout);
        
        return response;
    }
}