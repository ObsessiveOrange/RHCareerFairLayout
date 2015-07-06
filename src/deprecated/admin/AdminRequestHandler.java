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

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import adt.Table;
import adt.Term;
import adt.Workbook;
import adt.wrappers.TableArray;
import common.Response;
import common.Response.FailResponse;
import common.Response.SuccessResponse;
import managers.DataManager;
import managers.SQLManager;
import misc.DataTable;
import misc.Utils;
import servlets.ServletLog;

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
			respObj.put(name, Streams.asString(stream));
		    } else {
			String fileExt = item.getName().substring(item.getName().lastIndexOf('.'));
			if (fileExt.equalsIgnoreCase(".xls") || fileExt.equalsIgnoreCase(".xlsx")) {
			    Workbook workbook = new Workbook();
			    if (fileExt.equalsIgnoreCase(".xls")) {

				// Create Workbook instance holding reference to
				// .xls file
				HSSFWorkbook inputWorkbook = new HSSFWorkbook(stream);

				workbook.importFromWorkbook(inputWorkbook, true);

				inputWorkbook.close();
				stream.close();
			    } else if (fileExt.equalsIgnoreCase(".xlsx")) {

				// Create Workbook instance holding reference to
				// .xlsx file
				XSSFWorkbook inputWorkbook = new XSSFWorkbook(stream);

				workbook.importFromWorkbook(inputWorkbook, true);

				inputWorkbook.close();
				stream.close();
			    }
			    respObj.put("uploadedWorkbook", workbook);
			} else {
			    DataTable arr = new DataTable();
			    // respObj.put(name, Streams.asString(stream));
			    respObj.put("Item " + i,
				    "File field '" + name + "' with file name '" + item.getName() + "'");
			    // Process the input stream
			    arr.importFromFile(new BufferedReader(new InputStreamReader(stream)), "\t", true, "\"");

			    respObj.put(name, arr);
			}
		    }
		    i++;
		}
		return respObj;
	    } catch (Exception e) {
		return new FailResponse(e);
	    }
	}
	return new SuccessResponse();
    }

    @SuppressWarnings("unchecked")
    public static Response uploadData(String year, String quarter, Workbook uploadedWorkbook) {

	String dbName = Utils.getDBName(year, quarter);

	try {
	    if (!DataManager.checkDBExists(year, quarter)) {
		Response resp = createNewTerm(year, quarter);
		if (resp.get("success", Integer.class) != 1) {
		    return new FailResponse("Could not create new term");
		}
	    }

	    Response updateTermVarsResponse = DataManager.updateTermVars(dbName,
		    uploadedWorkbook.getSheet("Variables"));
	    if (!updateTermVarsResponse.isSuccess()) {
		FailResponse failResponse = new FailResponse("Failed updating TermVars");
		failResponse.put("updateTermVarsResponse", updateTermVarsResponse);
	    }
	    Response updateCategoriesAndCompaniesResponse = DataManager.updateCategoriesAndCompanies(dbName,
		    uploadedWorkbook.getSheet("Categories"), uploadedWorkbook.getSheet("Companies"));
	    if (!updateCategoriesAndCompaniesResponse.isSuccess()) {
		FailResponse failResponse = new FailResponse("Failed updating Categories and Companies");
		failResponse.put("updateCategoriesAndCompaniesResponse", updateCategoriesAndCompaniesResponse);
	    }
	    Response updateTableMappingsResponse = DataManager.updateTableMappings(dbName,
		    uploadedWorkbook.getSheet("TableMappings"),
		    updateCategoriesAndCompaniesResponse.get("companyList", List.class));
	    if (!updateTableMappingsResponse.isSuccess()) {
		FailResponse failResponse = new FailResponse("Failed updating Table Mappings");
		failResponse.put("updateTableMappingsResponse", updateTableMappingsResponse);
	    }

	    return new SuccessResponse("Term data successfully uploaded.");
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    public static Response createNewTerm(String year, String quarter) {

	try {

	    String dbName = Utils.getDBName(year, quarter);

	    // Create new database
	    PreparedStatement stmt = SQLManager.getConn()
		    .prepareStatement("CREATE DATABASE IF NOT EXISTS " + dbName + ";");
	    stmt.executeUpdate();

	    stmt = SQLManager.getConn().prepareStatement("INSERT INTO Terms (year, quarter) VALUES (?, ?);");
	    stmt.setString(1, year);
	    stmt.setString(2, quarter);
	    stmt.executeUpdate();

	    Statement newTermStatement = SQLManager.getConn(dbName).createStatement();
	    newTermStatement.executeUpdate("CREATE TABLE IF NOT EXISTS Categories (" + "id INT NOT NULL,"
		    + "name VARCHAR(100) NOT NULL," + "type VARCHAR(50) NOT NULL," + "PRIMARY KEY (id),"
		    + "UNIQUE (name, type)" + ")ENGINE=INNODB;");

	    newTermStatement.executeUpdate("CREATE TABLE IF NOT EXISTS Companies (" + "id INT NOT NULL,"
		    + "name VARCHAR(100) NOT NULL," + "description TEXT," + "PRIMARY KEY (id)" + ")ENGINE=INNODB;");

	    newTermStatement.executeUpdate(
		    "CREATE TABLE IF NOT EXISTS Representatives (" + "id INT NOT NULL," + "name VARCHAR(50) NOT NULL,"
			    + "roseGrad BOOLEAN NOT NULL," + "PRIMARY KEY (id)" + ")ENGINE=INNODB;");

	    newTermStatement.executeUpdate("CREATE TABLE IF NOT EXISTS Categories_Companies ("
		    + "categoryId int NOT NULL," + "companyId int NOT NULL," + "PRIMARY KEY (categoryId, companyId),"
		    + "FOREIGN KEY (categoryId) REFERENCES Categories(id) ON UPDATE CASCADE ON DELETE CASCADE,"
		    + "FOREIGN KEY (companyId) REFERENCES Companies(id) ON UPDATE CASCADE ON DELETE CASCADE"
		    + ")ENGINE=INNODB;");

	    newTermStatement.executeUpdate("CREATE TABLE IF NOT EXISTS Companies_Representatives ("
		    + "companyId INT NOT NULL," + "repId INT NOT NULL," + "PRIMARY KEY (companyId, repId),"
		    + "FOREIGN KEY (companyId) REFERENCES Companies(id) ON UPDATE CASCADE ON DELETE CASCADE,"
		    + "FOREIGN KEY (repId) REFERENCES Representatives(id) ON UPDATE CASCADE ON DELETE CASCADE"
		    + ")ENGINE=INNODB;");

	    newTermStatement.executeUpdate("CREATE TABLE IF NOT EXISTS UserCompanyList ("
		    + "username VARCHAR(30) NOT NULL," + "companyId INT NOT NULL," + "priority INT NOT NULL,"
		    + "PRIMARY KEY (username, companyId),"
		    + "FOREIGN KEY (username) REFERENCES RHCareerFairLayout.Users(username) ON UPDATE CASCADE ON DELETE CASCADE,"
		    + "FOREIGN KEY (companyId) REFERENCES Companies(id) ON UPDATE CASCADE ON DELETE CASCADE"
		    + ")ENGINE=INNODB;");

	    newTermStatement.executeUpdate("CREATE TABLE IF NOT EXISTS TableMappings (" + "tableNumber INT NOT NULL,"
		    + "companyId INT," + "tableSize INT NOT NULL DEFAULT 1," + "PRIMARY KEY (tableNumber),"
		    + "FOREIGN KEY (companyId) REFERENCES Companies(id) ON UPDATE CASCADE ON DELETE CASCADE"
		    + ")ENGINE=INNODB;");

	    newTermStatement.executeUpdate("CREATE TABLE IF NOT EXISTS TermVars (" + "item VARCHAR(50) NOT NULL,"
		    + "value VARCHAR(100) NOT NULL," + "type VARCHAR(30) NOT NULL," + "PRIMARY KEY (item)"
		    + ")ENGINE=INNODB;");

	    newTermStatement.executeUpdate("INSERT INTO " + dbName + ".TermVars" + "(item, value, type) " + "VALUES "
		    + "('Year','" + year + "', 'term')," + "('Term','" + quarter + "', 'term');");

	    return new SuccessResponse("Creation of new term: " + quarter + " " + year + " successful");
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

	    PreparedStatement updateTermRequestStatement = SQLManager.getConn().prepareStatement(
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
	    response.put("terms", terms);

	    return response;
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);

	}
    }

    public static Response updateTableMappingsHandler(TableArray mappings) {

	try {

	    PreparedStatement stmt;
	    ResultSet rs = null;

	    // Organize categories into hashmap
	    stmt = SQLManager.getConn(DataManager.getSelectedTerm()).prepareStatement(
		    "INSERT INTO TableMappings (tableNumber, companyId, tableSize) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE companyId = values(companyId), tableSize = values(tableSize)");

	    for (Table m : mappings) {

		stmt.setLong(1, m.getId());
		if (m.getCompanyId() == null) {
		    stmt.setNull(2, java.sql.Types.INTEGER);

		} else {
		    stmt.setLong(2, m.getCompanyId());

		}
		stmt.setInt(3, m.getTableSize());
		stmt.executeUpdate();
	    }

	    return new SuccessResponse("Table Mappings successfully updated");
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);

	}
    }
}
