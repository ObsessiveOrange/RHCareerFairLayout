package misc;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import common.Result;
import common.Result.FailResult;
import common.Result.SuccessResult;

public class Utils {

    public static String sanitizeString(String input) {

	return input.replaceAll("\\W", "");
    }

    public static Result validateStrings(Integer minLength, Integer maxLength, HttpServletRequest request,
	    String... headers) {

	SuccessResult resp = new SuccessResult();

	for (String h : headers) {
	    String s = request.getHeader(h);

	    if (s == null) {
		return new FailResult(400, "Invalid value provided for required header " + h + ".");
	    } else if (minLength != null && s.length() < minLength) {
		return new FailResult(400, "Invalid value provided for required header " + h + ".");
	    } else if (maxLength != null && s.length() > maxLength) {
		return new FailResult(400, "Invalid value provided for required header " + h + ".");
	    } else {
		resp.put(h, s);
	    }
	}
	return resp;
    }

    public static Result validateIntegers(Integer minValue, Integer maxValue, HttpServletRequest request,
	    String... headers) {

	SuccessResult resp = new SuccessResult();
	for (String h : headers) {
	    Integer value;

	    try {
		value = Integer.valueOf(request.getHeader(h));
	    } catch (NumberFormatException e) {
		return new FailResult(400, "Invalid value provided for required header " + h + ".");
	    }

	    if (value == null) {
		return new FailResult(400, "Invalid value provided for required header " + h + ".");
	    } else if (minValue != null && value < minValue) {
		return new FailResult(400, "Invalid value provided for required header " + h + ".");
	    } else if (maxValue != null && value > maxValue) {
		return new FailResult(400, "Invalid value provided for required header " + h + ".");
	    } else {
		resp.put(h, value);
	    }
	}
	return resp;
    }

    public static Result validateDoubles(Double minValue, Double maxValue, HttpServletRequest request,
	    String... headers) {

	SuccessResult resp = new SuccessResult();
	for (String h : headers) {
	    Double value;

	    try {
		value = Double.valueOf(request.getHeader(h));
	    } catch (NumberFormatException e) {
		return new FailResult(400, "Invalid value provided for required header " + h + ".");
	    }

	    if (value == null || value == Double.NaN || value == Double.NEGATIVE_INFINITY
		    || value == Double.POSITIVE_INFINITY) {
		return new FailResult(400, "Invalid value provided for required header " + h + ".");
	    } else if (minValue != null && value < minValue) {
		return new FailResult(400, "Invalid value provided for required header " + h + ".");
	    } else if (maxValue != null && value > maxValue) {
		return new FailResult(400, "Invalid value provided for required header " + h + ".");
	    } else {
		resp.put(h, value);
	    }
	}
	return resp;
    }

    public static Result validateNotNull(String name, Object obj) {

	if (obj == null) {
	    return new FailResult(400, "Missing required parameter '" + name + "'.");
	}
	return new SuccessResult();
    }

    public static Result validateTerm(String year, String quarter) {

	if (!(year.matches("\\d{4}") && quarter.matches("(?i:Spring|Fall|Winter)"))) {
	    return new FailResult(400, "Invalid year/quarter format.");
	}
	return new SuccessResult();
    }

    public static String toCamelCase(String s) {

	String[] parts = s.split("_");
	StringBuilder camelCaseString = new StringBuilder();
	for (String part : parts) {
	    camelCaseString.append(toProperCase(part));
	}
	return camelCaseString.toString();
    }

    public static String toCamelCaseWithUnderscore(String s) {

	String[] parts = s.split("_");
	StringBuilder camelCaseString = new StringBuilder();
	for (int i = 0; i < parts.length; i++) {
	    camelCaseString.append(toProperCase(parts[i]));
	    if (i < parts.length - 1) {
		camelCaseString.append("_");
	    }
	}
	return camelCaseString.toString();
    }

    public static String toProperCase(String s) {

	return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static String getDBName(String year, String quarter) {

	return year + "_" + toProperCase(quarter);
    }

    public static Result checkResultSuccess(ResultSet rs, int failCode) throws SQLException {
	if (hasColumn(rs, "status") && rs.next() && rs.getInt("status") < 0) {
	    if (hasColumn(rs, "message")) {
		return new FailResult(failCode, rs.getString("message"));
	    }
	    return new FailResult(500, "Failed, but no SQL Error given");
	}

	return new SuccessResult();
    }

    public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {

	ResultSetMetaData rsmd = rs.getMetaData();
	int columns = rsmd.getColumnCount();
	for (int x = 1; x <= columns; x++) {
	    if (columnName.equals(rsmd.getColumnName(x))) {
		return true;
	    }
	}
	return false;
    }

    public static ResultSet getNextResultSet(CallableStatement stmt) throws SQLException {
	boolean haveMoreResultSets = stmt.getMoreResults();
	while (true) {
	    if (!haveMoreResultSets && stmt.getUpdateCount() == -1) {
		return null;
	    } else if (haveMoreResultSets) {
		return stmt.getResultSet();
	    }
	    haveMoreResultSets = stmt.getMoreResults();
	}
    }
}
