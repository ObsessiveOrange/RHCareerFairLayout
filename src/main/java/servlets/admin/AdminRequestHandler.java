package servlets.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import managers.SQLManager;
import misc.ArrayList2D;
import misc.Utils;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import servlets.ServletLog;
import servlets.ServletLog.LogEvent;
import adt.LayoutVars;
import adt.Response;
import adt.Response.FailResponse;
import adt.Response.SuccessResponse;

public class AdminRequestHandler {
    
    public static Response handleUploadRequest(HttpServletRequest request) {
    
        Response respObj = new SuccessResponse("File Upload successful");
        
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            
            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload();
            
            try {
                // Parse the request
                FileItemIterator iter = upload.getItemIterator(request);
                int i = 0;
                while (iter.hasNext()) {
                    FileItemStream item = iter.next();
                    String name = item.getFieldName();
                    InputStream stream = item.openStream();
                    if (item.isFormField()) {
                        respObj.addToReturnData(name, Streams.asString(stream));
                    }
                    else {
                        if (item.getName().substring(item.getName().length() - 4).equalsIgnoreCase(".xls")) {
                            // || item.getName().substring(item.getName().length() - 5).equalsIgnoreCase(".xlsx")) {
                            FileInputStream file = new FileInputStream(new File("howtodoinjava_demo.xlsx"));
                            
                            // Create Workbook instance holding reference to .xls file
                            HSSFWorkbook workbook = new HSSFWorkbook(file);
                            
                            // Get first/desired sheet from the workbook
                            HSSFSheet sheet = workbook.getSheetAt(0);
                            
                            // Iterate through each rows one by one
                            Iterator<Row> rowIterator = sheet.iterator();
                            while (rowIterator.hasNext())
                            {
                                Row row = rowIterator.next();
                                // For each row, iterate through all the columns
                                Iterator<Cell> cellIterator = row.cellIterator();
                                
                                while (cellIterator.hasNext())
                                {
                                    Cell cell = cellIterator.next();
                                    // Check the cell type and format accordingly
                                    switch (cell.getCellType())
                                    {
                                        case Cell.CELL_TYPE_NUMERIC:
                                            Double value = cell.getNumericCellValue();
                                            if (value == value.intValue()) {
                                                respObj.addToReturnData("(" + cell.getRowIndex() + ", " + cell.getColumnIndex() + ")",
                                                        value.intValue());
                                            }
                                            else
                                                respObj.addToReturnData("(" + cell.getRowIndex() + ", " + cell.getColumnIndex() + ")", value);
                                            break;
                                        case Cell.CELL_TYPE_STRING:
                                            respObj.addToReturnData("(" + cell.getRowIndex() + ", " + cell.getColumnIndex() + ")",
                                                    cell.getStringCellValue());
                                            break;
                                    }
                                }
                            }
                            workbook.close();
                            file.close();
                        }
                        else {
                            // respObj.addToReturnData(name, Streams.asString(stream));
                            respObj.addToReturnData("Item " + i, "File field '" + name + "' with file name '"
                                    + item.getName() + "'");
                            // Process the input stream
                            ArrayList2D arr = new ArrayList2D();
                            arr.importFromFile(new BufferedReader(new InputStreamReader(stream)), "\t", true, "\"");
                            
                            respObj.addToReturnData("Item " + i + " data", arr.toJson());
                        }
                    }
                    i++;
                }
                return respObj;
            } catch (Exception e) {
                return new FailResponse(e);
            }
        }
        return new FailResponse("Expected content of type multipart/form-data");
    }
    
    public static Response handleNewTermRequest(HttpServletRequest request) {
    
        try {
            
            String year = Utils.sanitizeString(request.getHeader("year"));
            String term = Utils.sanitizeString(request.getHeader("term"));
            String dbName = term + year;
            
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
                    + "PRIMARY KEY (categoryId, companyId),"
                    + "FOREIGN KEY (categoryId) REFERENCES Categories(id) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (companyId) REFERENCES Companies(id) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")ENGINE=INNODB;");
            
            insertResult += ", " + newCategoryStatement.executeUpdate("CREATE TABLE Companies_Representatives ("
                    + "companyId INT NOT NULL,"
                    + "repId INT NOT NULL,"
                    + "PRIMARY KEY (companyId, repId),"
                    + "FOREIGN KEY (companyId) REFERENCES Companies(id) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (repId) REFERENCES Representatives(id) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")ENGINE=INNODB;");
            
            insertResult += ", " + newCategoryStatement.executeUpdate("CREATE TABLE UserCompanyList ("
                    + "username VARCHAR(30) NOT NULL,"
                    + "companyId INT NOT NULL,"
                    + "priority INT NOT NULL,"
                    + "PRIMARY KEY (username, companyId),"
                    + "FOREIGN KEY (username) REFERENCES Users.Users(username) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (companyId) REFERENCES Companies(id) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")ENGINE=INNODB;");
            
            insertResult += ", " + newCategoryStatement.executeUpdate("CREATE TABLE " + dbName + " ("
                    + "item VARCHAR(20) NOT NULL,"
                    + "value VARCHAR(20) NOT NULL"
                    + "PRIMARY KEY (item)"
                    + ")ENGINE=INNODB;");
            
            insertResult += ", " + newCategoryStatement.executeUpdate("INSERT INTO " + dbName
                    + "(item, value) "
                    + "VALUES "
                    + "('Year'," + year + "),"
                    + "('Term'," + term + "),"
                    + ")ENGINE=INNODB;");
            
            return new SuccessResponse("Rows changed: " + insertResult);
        } catch (SQLException e) {
            LogEvent event = new LogEvent();
            event.setDetail("Type", "Exception");
            event.setDetail("Exception", e.getStackTrace());
            ServletLog.logEvent(event);
            
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
