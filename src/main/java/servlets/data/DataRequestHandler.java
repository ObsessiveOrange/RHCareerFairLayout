package servlets.data;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import adt.Category;
import adt.Company;
import adt.DataVars;
import adt.LayoutVars;
import adt.Response;
import adt.Response.SuccessResponse;

public class DataRequestHandler {
    
    public static Response handleGetDataRequest(HttpServletRequest request) throws IOException {
    
        HashMap<String, HashMap<Integer, Category>> categoryMap = new HashMap<String, HashMap<Integer, Category>>();
        HashMap<Integer, Company> companyMap = DataServlet.companyMap;
        LayoutVars layout = DataServlet.layoutVars;
        
        for (String type : DataVars.getAllTypes()) {
            HashMap<Integer, Category> newCategoryMap = new HashMap<Integer, Category>();
            categoryMap.put(type, newCategoryMap);
            for (Integer id : DataVars.getAllOfType(type)) {
                newCategoryMap.put(id, DataServlet.categoryMap.get(id));
            }
        }
        
        SuccessResponse response = new SuccessResponse();
        response.addToReturnData("title", "Career Fair " + DataVars.getQuarter() + " "
                + DataVars.getYear());
        response.addToReturnData("categories", categoryMap);
        response.addToReturnData("companies", companyMap);
        response.addToReturnData("layout", layout);
        
        return response;
    }
}