package servlets.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import servlets.SystemVars;
import adt.CachedResult;
import adt.Category;
import adt.Company;
import adt.LayoutVars;
import adt.Response;
import adt.Response.SuccessResponse;

public class DataRequestHandler {
    
    private static CachedResult cachedData = null;
    private static String       adminKey   = "M4Z-Z#hA=NDL.p^E93=3NO;8vO]uFF";
    
    private static Connection   conn       = null;
    
    public static boolean setupDataRequestHandler() {
    
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            conn = DriverManager.getConnection("jdbc:mysql://" + SystemVars.getDbhost() + ":" + SystemVars.getDbport() + "/" + SystemVars.getTerm(),
                    SystemVars.getDbusername(), SystemVars.getDbpassword());
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static Response handleStartRequest(HttpServletRequest request) {
    
        return new SuccessResponse("Ready to serve requests");
    }
    
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
    
    public static Response handleForceRegenerationOfData(HttpServletRequest request) {
    
        cachedData = null;
        
        return new SuccessResponse("Cached data removed - will regenerate on next request");
    }
}
// public static Response handleTestDbRequest(HttpServletRequest request) {
//
// StringBuilder s = new StringBuilder();
// PreparedStatement prepStatement = null;
// try {
//
// prepStatement = conn.prepareStatement("SELECT * FROM comments;");
//
// ResultSet result = prepStatement.executeQuery();
//
// List<Map<String, String>> readResult = new ArrayList<Map<String, String>>();
// while (result.next()) {
// Map<String, String> lineResult = new HashMap<String, String>();
// lineResult.put("id", result.getString("id"));
// lineResult.put("MYUSER", result.getString("MYUSER"));
// lineResult.put("EMAIL", result.getString("EMAIL"));
// lineResult.put("WEBPAGE", result.getString("WEBPAGE"));
// lineResult.put("DATUM", result.getString("DATUM"));
// lineResult.put("SUMMARY", result.getString("SUMMARY"));
// lineResult.put("COMMENTS", result.getString("COMMENTS"));
// readResult.add(lineResult);
// }
// result.close();
//
// prepStatement =
// conn.prepareStatement("INSERT INTO comments (MYUSER, EMAIL, WEBPAGE, DATUM, SUMMARY, COMMENTS) VALUES (?, ?, ?, ?, ?, ?);");
// prepStatement.setString(1, "ben");
// prepStatement.setString(2, "ben@gmail.com");
// prepStatement.setString(3, "http://www.ben.com");
// prepStatement.setDate(4, new Date(System.currentTimeMillis()));
// prepStatement.setString(5, "BLAH.");
// prepStatement.setString(6, "Test");
// Integer insertResult = prepStatement.executeUpdate();
//
// HashMap<String, Object> returnMap = new HashMap<String, Object>();
// returnMap.put("success", 1);
// returnMap.put("instruction", prepStatement.toString());
// returnMap.put("read code", readResult);
// returnMap.put("insert code", insertResult);
// returnMap.put("timestamp", System.currentTimeMillis());
//
// return new Gson().toJson(returnMap);
// } catch (Exception e) {
// s.append("Error: ");
// for (StackTraceElement element : e.getStackTrace()) {
// s.append(element.toString());
// s.append("\n");
// }
//
// HashMap<String, Object> returnMap = new HashMap<String, Object>();
// returnMap.put("success", 1);
// returnMap.put("instruction", prepStatement != null ? prepStatement.toString() : "null");
// returnMap.put("error", s.toString());
// returnMap.put("timestamp", System.currentTimeMillis());
// return new Gson().toJson(returnMap);
// }
// }