package servlets.admin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import managers.DataManager;
import managers.SQLManager;
import misc.DataTable;
import misc.Utils;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import servlets.ServletLog;
import adt.Response;
import adt.Response.FailResponse;
import adt.Response.SuccessResponse;
import adt.Term;
import adt.Workbook;

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
                        String fileExt = item.getName().substring(item.getName().lastIndexOf('.'));
                        if (fileExt.equalsIgnoreCase(".xls") || fileExt.equalsIgnoreCase(".xlsx")) {
                            Workbook workbook = new Workbook();
                            if (fileExt.equalsIgnoreCase(".xls")) {
                                
                                // Create Workbook instance holding reference to .xls file
                                HSSFWorkbook inputWorkbook = new HSSFWorkbook(stream);
                                
                                workbook.importFromWorkbook(inputWorkbook, true);
                                
                                inputWorkbook.close();
                                stream.close();
                            }
                            else if (fileExt.equalsIgnoreCase(".xlsx")) {
                                
                                // Create Workbook instance holding reference to .xlsx file
                                XSSFWorkbook inputWorkbook = new XSSFWorkbook(stream);
                                
                                workbook.importFromWorkbook(inputWorkbook, true);
                                
                                inputWorkbook.close();
                                stream.close();
                            }
                            respObj.addToReturnData("uploadedWorkbook", workbook);
                        }
                        else {
                            DataTable arr = new DataTable();
                            // respObj.addToReturnData(name, Streams.asString(stream));
                            respObj.addToReturnData("Item " + i, "File field '" + name + "' with file name '"
                                    + item.getName() + "'");
                            // Process the input stream
                            arr.importFromFile(new BufferedReader(new InputStreamReader(stream)), "\t", true, "\"");
                            
                            respObj.addToReturnData(name, arr);
                        }
                    }
                    i++;
                }
                return respObj;
            } catch (Exception e) {
                return new FailResponse(e);
            }
        }
        return new FailResponse(-100, "Expected content of type multipart/form-data");
    }
    
    @SuppressWarnings("unchecked")
    public static Response uploadData(String year, String quarter, Workbook uploadedWorkbook) {
    
        String dbName = Utils.getDBName(year, quarter);
        
        try {
            if (!DataManager.checkDBExists(year, quarter)) {
                Response resp = createNewTerm(year, quarter);
                if (resp.getFromReturnData("success", Integer.class) != 1) {
                    return new FailResponse("Could not create new term");
                }
            }
            
            Response updateTermVarsResponse = DataManager.updateTermVars(dbName, uploadedWorkbook.getSheet("Variables"));
            if (!updateTermVarsResponse.success) {
                FailResponse failResponse = new FailResponse("Failed updating TermVars");
                failResponse.addToReturnData("updateTermVarsResponse", updateTermVarsResponse);
            }
            Response updateCategoriesAndCompaniesResponse =
                    DataManager.updateCategoriesAndCompanies(dbName, uploadedWorkbook.getSheet("Categories"), uploadedWorkbook.getSheet("Companies"));
            if (!updateCategoriesAndCompaniesResponse.success) {
                FailResponse failResponse = new FailResponse("Failed updating Categories and Companies");
                failResponse.addToReturnData("updateCategoriesAndCompaniesResponse", updateCategoriesAndCompaniesResponse);
            }
            Response updateTableMappingsResponse =
                    DataManager.updateTableMappings(dbName, uploadedWorkbook.getSheet("TableMappings"),
                            updateCategoriesAndCompaniesResponse.getFromReturnData("companyList", List.class));
            if (!updateTableMappingsResponse.success) {
                FailResponse failResponse = new FailResponse("Failed updating Table Mappings");
                failResponse.addToReturnData("updateTableMappingsResponse", updateTableMappingsResponse);
            }
            return new SuccessResponse("Term data uploaded successfully.");
        } catch (Exception e) {
            ServletLog.logEvent(e);
            
            return new FailResponse(e);
        }
    }
    
    public static Response createNewTerm(String year, String quarter) {
    
        try {
            
            String dbName = Utils.getDBName(year, quarter);
            
            // Create new database
            PreparedStatement stmt = SQLManager.getConn().prepareStatement("CREATE DATABASE IF NOT EXISTS " + dbName + ";");
            stmt.executeUpdate();
            
            stmt = SQLManager.getConn().prepareStatement("INSERT INTO Terms (year, quarter) VALUES (?, ?);");
            stmt.setString(1, year);
            stmt.setString(2, quarter);
            stmt.executeUpdate();
            
            Statement newTermStatement = SQLManager.getConn(dbName).createStatement();
            newTermStatement.executeUpdate("CREATE TABLE IF NOT EXISTS Categories ("
                    + "id INT NOT NULL,"
                    + "name VARCHAR(100) NOT NULL,"
                    + "type VARCHAR(50) NOT NULL,"
                    + "PRIMARY KEY (id),"
                    + "UNIQUE (name, type)"
                    + ")ENGINE=INNODB;");
            
            newTermStatement.executeUpdate("CREATE TABLE IF NOT EXISTS Companies ("
                    + "id INT NOT NULL,"
                    + "name VARCHAR(100) NOT NULL,"
                    + "tableNo INT,"
                    + "description TEXT,"
                    + "PRIMARY KEY (id)"
                    + ")ENGINE=INNODB;");
            
            newTermStatement.executeUpdate("CREATE TABLE IF NOT EXISTS Representatives ("
                    + "id INT NOT NULL,"
                    + "name VARCHAR(50) NOT NULL,"
                    + "roseGrad BOOLEAN NOT NULL,"
                    + "PRIMARY KEY (id)"
                    + ")ENGINE=INNODB;");
            
            newTermStatement.executeUpdate("CREATE TABLE IF NOT EXISTS Categories_Companies ("
                    + "categoryId int NOT NULL,"
                    + "companyId int NOT NULL,"
                    + "PRIMARY KEY (categoryId, companyId),"
                    + "FOREIGN KEY (categoryId) REFERENCES Categories(id) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (companyId) REFERENCES Companies(id) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")ENGINE=INNODB;");
            
            newTermStatement.executeUpdate("CREATE TABLE IF NOT EXISTS Companies_Representatives ("
                    + "companyId INT NOT NULL,"
                    + "repId INT NOT NULL,"
                    + "PRIMARY KEY (companyId, repId),"
                    + "FOREIGN KEY (companyId) REFERENCES Companies(id) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (repId) REFERENCES Representatives(id) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")ENGINE=INNODB;");
            
            newTermStatement.executeUpdate("CREATE TABLE IF NOT EXISTS UserCompanyList ("
                    + "username VARCHAR(30) NOT NULL,"
                    + "companyId INT NOT NULL,"
                    + "priority INT NOT NULL,"
                    + "PRIMARY KEY (username, companyId),"
                    + "FOREIGN KEY (username) REFERENCES RHCareerFairLayout.Users(username) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (companyId) REFERENCES Companies(id) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")ENGINE=INNODB;");
            
            newTermStatement.executeUpdate("CREATE TABLE IF NOT EXISTS TableMappings ("
                    + "tableNumber INT NOT NULL,"
                    + "companyId INT NOT NULL,"
                    + "tableSize INT NOT NULL,"
                    + "PRIMARY KEY (location),"
                    + "FOREIGN KEY (companyId) REFERENCES Companies(id) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")ENGINE=INNODB;");
            
            newTermStatement.executeUpdate("CREATE TABLE IF NOT EXISTS TermVars ("
                    + "item VARCHAR(50) NOT NULL,"
                    + "value VARCHAR(100) NOT NULL,"
                    + "type VARCHAR(30) NOT NULL,"
                    + "PRIMARY KEY (item)"
                    + ")ENGINE=INNODB;");
            
            newTermStatement.executeUpdate("INSERT INTO " + dbName + ".TermVars"
                    + "(item, value, type) "
                    + "VALUES "
                    + "('Year','" + year + "', 'term'),"
                    + "('Term','" + quarter + "', 'term');");
            
            return new SuccessResponse("Creation of new term: " + year + " " + quarter + " successful");
        } catch (Exception e) {
            ServletLog.logEvent(e);
            
            return new FailResponse(e);
        }
        
    }
    
    public static Response setTerm(String year, String quarter) {
    
        try {
            if (!DataManager.checkDBExists(year, quarter)) {
                return new FailResponse("Invalid term selected.");
            }
            
            PreparedStatement updateTermRequestStatement = SQLManager.getConn("RHCareerFairLayout").prepareStatement(
                    "INSERT INTO Vars (item, value, type) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value=values(value), type=values(type);");
            
            updateTermRequestStatement.setString(1, "selectedYear");
            updateTermRequestStatement.setString(2, year);
            updateTermRequestStatement.setString(3, "selectedTerm");
            updateTermRequestStatement.executeUpdate();
            
            updateTermRequestStatement.setString(1, "selectedQuarter");
            updateTermRequestStatement.setString(2, quarter);
            updateTermRequestStatement.setString(3, "selectedTerm");
            updateTermRequestStatement.executeUpdate();
            
            DataManager.setSelectedTerm(year, quarter);
            
            return new SuccessResponse("Term successfully updated");
        } catch (Exception e) {
            ServletLog.logEvent(e);
            
            return new FailResponse(e);
        }
    }
    
    public static Response listTerms() {
    
        ResultSet rs = null;
        try {
            
            // Organize categories into hashmap
            rs = SQLManager.getConn().createStatement().executeQuery("SELECT year, quarter FROM Terms;");
            
            List<Term> terms = new ArrayList<Term>();
            
            while (rs.next()) {
                String year = rs.getString("year");
                String quarter = rs.getString("quarter");
                
                terms.add(new Term(year, quarter));
            }
            
            Collections.sort(terms);
            
            SuccessResponse response = new SuccessResponse();
            response.addToReturnData("terms", terms);
            
            return response;
        } catch (Exception e) {
            ServletLog.logEvent(e);
            
            return new FailResponse(e);
            
        }
    }
}
