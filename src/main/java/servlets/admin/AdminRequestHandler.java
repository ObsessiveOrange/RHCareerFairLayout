package servlets.admin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;

import managers.SQLManager;
import misc.Utils;
import adt.LayoutVars;
import adt.Response;
import adt.Response.FailResponse;
import adt.Response.SuccessResponse;

public class AdminRequestHandler {
    
    public static Response handleNewTermRequest(HttpServletRequest request) {
    
        try {
            
            String dbName = Utils.sanitizeString(request.getHeader("term"));
            
            // Create new database
            PreparedStatement createDatabaseStatement = SQLManager.getConn("mysql").prepareStatement("CREATE DATABASE " + dbName + ";");
            String insertResult = createDatabaseStatement.executeUpdate() + "";
            
            Statement newCategoryStatement = SQLManager.getConn(dbName).createStatement();
            insertResult += ", " + newCategoryStatement.executeUpdate("CREATE TABLE Categories ("
                    + "id INT NOT NULL AUTO_INCREMENT,"
                    + "title VARCHAR(50) NOT NULL,"
                    + "type VARCHAR(25) NOT NULL,"
                    + "PRIMARY KEY (id)"
                    + ")ENGINE=INNODB;");
            
            insertResult += ", " + newCategoryStatement.executeUpdate("CREATE TABLE Companies ("
                    + "id INT NOT NULL AUTO_INCREMENT,"
                    + "name VARCHAR(50) NOT NULL,"
                    + "description TEXT,"
                    + "PRIMARY KEY (id)"
                    + ")ENGINE=INNODB;");
            
            insertResult += ", " + newCategoryStatement.executeUpdate("CREATE TABLE Representatives ("
                    + "id INT NOT NULL AUTO_INCREMENT,"
                    + "name VARCHAR(50) NOT NULL,"
                    + "roseGrad BOOLEAN NOT NULL,"
                    + "PRIMARY KEY (id)"
                    + ")ENGINE=INNODB;");
            
            insertResult += ", " + newCategoryStatement.executeUpdate("CREATE TABLE Categories_Companies ("
                    + "categoryId int NOT NULL,"
                    + "companyId int NOT NULL,"
                    + "FOREIGN KEY (categoryId) REFERENCES Categories(id) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (companyId) REFERENCES Companies(id) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")ENGINE=INNODB;");
            
            insertResult += ", " + newCategoryStatement.executeUpdate("CREATE TABLE Companies_Representatives ("
                    + "companyId INT NOT NULL,"
                    + "repId INT NOT NULL,"
                    + "FOREIGN KEY (companyId) REFERENCES Companies(id) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (repId) REFERENCES Representatives(id) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")ENGINE=INNODB;");
            
            insertResult += ", " + newCategoryStatement.executeUpdate("CREATE TABLE UserCompanyList ("
                    + "username VARCHAR(30) NOT NULL,"
                    + "companyId INT NOT NULL,"
                    + "priority INT NOT NULL,"
                    + "FOREIGN KEY (username) REFERENCES Users.Users(username) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (companyId) REFERENCES Companies(id) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")ENGINE=INNODB;");
            
            return new SuccessResponse("Rows changed: " + insertResult);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new FailResponse(e.toString());
        }
        
    }
    
    public static Response handleSetSizeRequest(HttpServletRequest request) {
    
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