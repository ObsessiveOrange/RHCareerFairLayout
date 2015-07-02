package managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import common.Response;
import common.Response.FailResponse;
import common.Response.SuccessResponse;
import misc.BCrypt;
import servlets.ServletLog;

public class AuthManager {

    private static final int SESSION_VALID_DAYS = 15;

    public static Response addUser(HttpServletRequest request) {

	try {

	    String userName = request.getHeader("authUser");
	    String password = request.getHeader("authPass");

	    // check to make sure username does not already exist
	    PreparedStatement check = SQLManager.getConn("RHCareerFairLayout")
		    .prepareStatement("SELECT COUNT(username) FROM Users WHERE username = ?;");

	    check.setString(1, userName);
	    ResultSet users = check.executeQuery();
	    users.next();
	    if (users.getInt(1) != 0) {
		return new FailResponse("Username already exists");
	    }
	    users.close();
	    check.close();

	    // Permissions levels:
	    // 1 - Users (Edit saved companies, visit list)
	    // 10 - Admin (Edit user permssions, edit company/category list)
	    // Always add as users, require admin access to elevate
	    PreparedStatement addUser = SQLManager.getConn("RHCareerFairLayout")
		    .prepareStatement("INSERT INTO Users (username, hashedPw, permissions) VALUES (?, ?, 1);");
	    addUser.setString(1, userName);
	    addUser.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
	    Integer insertResult = addUser.executeUpdate();

	    return new SuccessResponse("Rows changed: " + insertResult);
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    public static Response checkToken(HttpServletRequest request) {

	try {

	    String userName = request.getHeader("authUser");
	    String token = request.getHeader("authToken");

	    if (request.getCookies() != null) {
		for (Cookie c : request.getCookies()) {
		    if (c.getName().equals("authUser")) {
			userName = c.getValue();
		    }
		    if (c.getName().equals("authToken")) {
			token = c.getValue();
		    }
		}
	    }

	    if (userName == null) {
		return new FailResponse("Username not provided");
	    }

	    if (token == null) {
		return new FailResponse("Token not provided");
	    }

	    PreparedStatement getAuthToken = SQLManager.getConn("RHCareerFairLayout").prepareStatement(
		    "SELECT sessionKey, sessionValidDate FROM Sessions WHERE username = ? AND sessionClient = ?;");
	    getAuthToken.setString(1, userName);
	    getAuthToken.setString(2, request.getHeader("User-Agent") == null ? "NO USER-AGENT PROVIDED"
		    : request.getHeader("User-Agent"));
	    ResultSet result = getAuthToken.executeQuery();

	    boolean hasNextResult;
	    while (hasNextResult = result.next()) {

		if (token.equals(result.getString("sessionKey"))
			&& result.getTimestamp("sessionValidDate").after(new Timestamp(System.currentTimeMillis()))) {
		    break;
		}
	    }
	    result.close();
	    if (!hasNextResult) {
		return new FailResponse("Invalid Token");
	    }

	    return new SuccessResponse();
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    public static Response logoutUser(HttpServletRequest request) {

	try {

	    String userName = request.getHeader("authUser");
	    String token = request.getHeader("authToken");

	    if (request.getCookies() != null) {
		for (Cookie c : request.getCookies()) {
		    if (c.getName().equals("authUser")) {
			userName = c.getValue();
		    }
		    if (c.getName().equals("authToken")) {
			token = c.getValue();
		    }
		}
	    }

	    if (userName == null) {
		return new FailResponse("Username not provided");
	    }

	    if (token == null) {
		return new FailResponse("Token not provided");
	    }

	    PreparedStatement getAuthToken = SQLManager.getConn("RHCareerFairLayout").prepareStatement(
		    "UPDATE Sessions SET sessionValidDate = ? WHERE username = ? AND sessionKey = ? AND sessionClient = ?;");
	    getAuthToken.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
	    getAuthToken.setString(2, userName);
	    getAuthToken.setString(3, token);
	    getAuthToken.setString(4, request.getHeader("User-Agent") == null ? "NO USER-AGENT PROVIDED"
		    : request.getHeader("User-Agent"));
	    getAuthToken.executeUpdate();

	    return new SuccessResponse();
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }
}
