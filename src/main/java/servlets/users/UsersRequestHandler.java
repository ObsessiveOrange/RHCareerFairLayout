package servlets.users;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import misc.BCrypt;
import servlets.SystemVars;
import adt.CachedResult;
import adt.Category;
import adt.Company;
import adt.LayoutVars;

import com.google.gson.Gson;

public class UsersRequestHandler {
    
    private static CachedResult cachedData = null;
    private static String       adminKey   = "M4Z-Z#hA=NDL.p^E93=3NO;8vO]uFF";
    
    private static Connection   conn       = null;
    
    public static boolean setupAdminRequestHandler() {
    
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            conn = DriverManager.getConnection("jdbc:mysql://" + SystemVars.getDbhost() + ":" + SystemVars.getDbport() + "/Users",
                    SystemVars.getDbusername(), SystemVars.getDbpassword());
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static String handleRegisterUserRequest(HttpServletRequest request) {
    
        try {
            // check to make sure username does not already exist
            PreparedStatement check = conn.prepareStatement("SELECT COUNT(id) FROM users WHERE username = '" + request.getHeader("authUser") + "';");
            ResultSet users = check.executeQuery();
            if (users.getInt(0) != 0) {
                return SystemVars.failString("Username already exists");
            }
            
            // Permissions levels:
            // 1 - Users (Edit saved companies, visit list)
            // 10 - Admin (Edit user permssions, edit company/category list)
            // Always add as users, require admin access to elevate
            PreparedStatement statement = conn.prepareStatement("INSERT INTO Users (username, hashedPw, permissions) VALUES (?, ?, 1);");
            statement.setString(1, request.getHeader("authUser"));
            statement.setString(2, BCrypt.hashpw(request.getHeader("authPass"), BCrypt.gensalt()));
            Integer insertResult = statement.executeUpdate();
            
            return SystemVars.successString("Rows changed: " + insertResult);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return SystemVars.failString(e.toString());
        }
        
    }
    
    public static String handleGetDataRequest(HttpServletRequest request) throws IOException {
    
        if (cachedData != null && cachedData.getCacheDate() >= (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10))) {
            System.out.println("Using cached data");
            return cachedData.getCachedResult();
        }
        
        System.out.println("Generating cached data");
        
        HashMap<Integer, Category> categoryMap = UsersServlet.categoryMap;
        HashMap<Integer, Company> entryMap = UsersServlet.entryMap;
        LayoutVars layout = UsersServlet.layoutVars;
        
        HashMap<String, Object> returnMap = new HashMap<String, Object>();
        
        returnMap.put("success", 1);
        returnMap.put("title", "Career Fair " + UsersServlet.dataVars.getQuarter() + " "
                + UsersServlet.dataVars.getYear());
        returnMap.put("categories", categoryMap);
        returnMap.put("entries", entryMap);
        returnMap.put("layout", layout);
        returnMap.put("timestamp", System.currentTimeMillis());
        
        cachedData = new CachedResult(new Gson().toJson(returnMap));
        
        return cachedData.getCachedResult();
    }
    
    public static String handleForceRegenerationOfData(HttpServletRequest request) {
    
        cachedData = null;
        
        return SystemVars.successString("Cached data removed - will regenerate on next request");
    }
    
    public static String handleSetSizeRequest(HttpServletRequest request) {
    
        // force regeneration of data:
        cachedData = null;
        
        if (request.getHeader("key") == null || !request.getHeader("key").equals(adminKey)) {
            return SystemVars.failString("Invalid key");
        }
        
        String section = request.getHeader("section");
        Integer size = request.getHeader("size") == null ? -1 : Integer.valueOf(request.getHeader("size"));
        if (section == null || size == -1) {
            return "{\"Server Timestamp\"" + System.currentTimeMillis() + ",\"Success\":0, \"Error\":\"Invalid section supplied\"}";
        }
        
        LayoutVars layout = UsersServlet.layoutVars;
        
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
                return SystemVars.failString("Invalid section provided");
        }
        
        return SystemVars.successString("Size successfully set");
    }
    
    public static String handleTestDbRequest(HttpServletRequest request) {
    
        StringBuilder s = new StringBuilder();
        PreparedStatement prepStatement = null;
        try {
            // Class.forName("com.mysql.jdbc.Driver");
            
            prepStatement = conn.prepareStatement("SELECT * FROM comments;");
            
            ResultSet result = prepStatement.executeQuery();
            
            List<Map<String, String>> readResult = new ArrayList<Map<String, String>>();
            while (result.next()) {
                Map<String, String> lineResult = new HashMap<String, String>();
                lineResult.put("id", result.getString("id"));
                lineResult.put("MYUSER", result.getString("MYUSER"));
                lineResult.put("EMAIL", result.getString("EMAIL"));
                lineResult.put("WEBPAGE", result.getString("WEBPAGE"));
                lineResult.put("DATUM", result.getString("DATUM"));
                lineResult.put("SUMMARY", result.getString("SUMMARY"));
                lineResult.put("COMMENTS", result.getString("COMMENTS"));
                readResult.add(lineResult);
            }
            
            prepStatement =
                    conn.prepareStatement("INSERT INTO comments (MYUSER, EMAIL, WEBPAGE, DATUM, SUMMARY, COMMENTS) VALUES (?, ?, ?, ?, ?, ?);");
            prepStatement.setString(1, "ben");
            prepStatement.setString(2, "ben@gmail.com");
            prepStatement.setString(3, "http://www.ben.com");
            prepStatement.setDate(4, new Date(System.currentTimeMillis()));
            prepStatement.setString(5, "BLAH.");
            prepStatement.setString(6, "Test");
            Integer insertResult = prepStatement.executeUpdate();
            
            HashMap<String, Object> returnMap = new HashMap<String, Object>();
            returnMap.put("success", 1);
            returnMap.put("instruction", prepStatement.toString());
            returnMap.put("read code", readResult);
            returnMap.put("insert code", insertResult);
            returnMap.put("timestamp", System.currentTimeMillis());
            
            return new Gson().toJson(returnMap);
        } catch (Exception e) {
            s.append("Error: ");
            for (StackTraceElement element : e.getStackTrace()) {
                s.append(element.toString());
                s.append("\n");
            }
            
            HashMap<String, Object> returnMap = new HashMap<String, Object>();
            returnMap.put("success", 1);
            returnMap.put("instruction", prepStatement != null ? prepStatement.toString() : "null");
            returnMap.put("error", s.toString());
            returnMap.put("timestamp", System.currentTimeMillis());
            return new Gson().toJson(returnMap);
        }
    }
    //
    // public static String handleGetDataRequest(HttpServletRequest request) throws IOException {
    //
    // long menuID = request.getParameter("category") == null ? 1 : Long.valueOf(request.getParameter("menuID"));
    //
    // ArrayList<Item> updatedItems = new ArrayList<Item>();
    //
    // getUpdatedItems(ofy().load().key(Key.create(Category.class, menuID)).now(), updatedItems);
    //
    // HashMap<String, Object> returnMap = new HashMap<String, Object>();
    //
    // returnMap.put("Success", 1);
    // returnMap.put("Updated Objects", updatedItems);
    // returnMap.put("Server Timestamp", System.currentTimeMillis());
    //
    // return new Gson().toJson(returnMap);
    // }
}
