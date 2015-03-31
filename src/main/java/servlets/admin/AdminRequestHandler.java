package servlets.admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import misc.BCrypt;
import servlets.SystemVars;
import adt.CachedResult;
import adt.LayoutVars;
import adt.Response;
import adt.Response.FailResponse;
import adt.Response.SuccessResponse;

public class AdminRequestHandler {
    
    private static CachedResult cachedData = null;
    private static String       adminKey   = "M4Z-Z#hA=NDL.p^E93=3NO;8vO]uFF";
    
    private static Connection   conn       = null;
    
    public static boolean setupAdminRequestHandler() {
    
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            conn = DriverManager.getConnection("jdbc:mysql://" + SystemVars.getDbhost() + ":" + SystemVars.getDbport(),
                    SystemVars.getDbusername(), SystemVars.getDbpassword());
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static Response handleNewTermRequest(HttpServletRequest request) {
    
        try {
            
            String dbName = request.getHeader("term").replaceAll("[^\\p{L}\\p{Nd}]+", "");
            
            // Create new database
            PreparedStatement statement = conn.prepareStatement("CREATE DATABASE " + dbName + ";");
            String insertResult = statement.executeUpdate() + "";
            
            statement = conn.prepareStatement("CREATE TABLE Categories (id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, "
                    + "title VARCHAR(50) NOT NULL"
                    + ")ENGINE=INNODB;");
            insertResult += ", " + statement.executeUpdate();
            
            return new SuccessResponse("Rows changed: " + insertResult);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new FailResponse(e.toString());
        }
        
    }
    
    public static Response handleLoginRequest(HttpServletRequest request) {
    
        try {
            
            String user = request.getHeader("authUser");
            String pass = request.getHeader("authPass");
            
            conn = DriverManager.getConnection("jdbc:mysql://" + SystemVars.getDbhost() + ":" + SystemVars.getDbport() + "/Users",
                    SystemVars.getDbusername(), SystemVars.getDbpassword());
            PreparedStatement prepStatement = conn.prepareStatement("SELECT hashedPw FROM Users WHERE username = '" + user + "';");
            
            ResultSet result = prepStatement.executeQuery();
            result.next();
            
            if (BCrypt.checkpw(pass, result.getString("hashedPw"))) {
                
            }
            
            return new SuccessResponse("success");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new FailResponse(e.toString());
        }
        
    }
    
    public static Response handleForceRegenerationOfData(HttpServletRequest request) {
    
        cachedData = null;
        
        return new SuccessResponse("Cached data removed - will regenerate on next request");
    }
    
    public static Response handleSetSizeRequest(HttpServletRequest request) {
    
        // force regeneration of data:
        cachedData = null;
        
        if (request.getHeader("key") == null || !request.getHeader("key").equals(adminKey)) {
            return new FailResponse("Invalid key");
        }
        
        String section = request.getHeader("section");
        Integer size = request.getHeader("size") == null ? -1 : Integer.valueOf(request.getHeader("size"));
        if (section == null || size == -1) {
            return new FailResponse("Invalid section provided");
        }
        
        LayoutVars layout = AdminServlet.layoutVars;
        
        switch (section.toLowerCase()) {
            case "1":
            case "section1":
                layout.setSection1(size);
                break;
            case "2":
            case "section2":
                layout.setSection2(size);
                break;
            case "2r":
            case "section2rows":
                layout.setSection2Rows(size);
                break;
            case "2p":
            case "section2pathwidth":
                layout.setSection2PathWidth(size);
                break;
            case "3":
            case "section3":
                layout.setSection3(size);
                break;
            default:
                return new FailResponse("Invalid section provided");
        }
        
        return new SuccessResponse("Size successfully set");
    }
    
    // public static Response handleTestDbRequest(HttpServletRequest request) {
    //
    // StringBuilder s = new StringBuilder();
    // PreparedStatement prepStatement = null;
    // try {
    // // Class.forName("com.mysql.jdbc.Driver");
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
}
