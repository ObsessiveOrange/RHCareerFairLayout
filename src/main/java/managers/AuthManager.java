package managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import misc.BCrypt;
import servlets.ServletLog;
import adt.Response;
import adt.Response.FailResponse;
import adt.Response.SuccessResponse;

public class AuthManager {
    
    private static final int SESSION_VALID_DAYS = 15;
    
    public static Response addUser(HttpServletRequest request) {
    
        try {
            
            String userName = request.getHeader("authUser");
            String password = request.getHeader("authPass");
            
            // check to make sure username does not already exist
            PreparedStatement check =
                    SQLManager.getConn("RHCareerFairLayout").prepareStatement("SELECT COUNT(username) FROM Users WHERE username = ?;");
            
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
            PreparedStatement addUser =
                    SQLManager.getConn("RHCareerFairLayout")
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
    
    public static Response authenticateUser(HttpServletRequest request) {
    
        try {
            
            // Response checkTokenResponse;
            // if ((checkTokenResponse = AuthManager.checkToken(request)).success) {
            // return checkTokenResponse;
            // }
            
            String userName = request.getHeader("authUser");
            String password = request.getHeader("authPass");
            
            if (userName == null || password == null) {
                return new FailResponse("Invalid Username/Password provided");
            }
            
            PreparedStatement getHashedPWStatement =
                    SQLManager.getConn("RHCareerFairLayout").prepareStatement("SELECT hashedPw FROM Users WHERE username = ?;");
            
            getHashedPWStatement.setString(1, userName);
            
            ResultSet result = getHashedPWStatement.executeQuery();
            boolean hasNext = result.next();
            
            if (!hasNext || !BCrypt.checkpw(password, result.getString("hashedPw"))) {
                return new FailResponse("Invalid Username/Password Combination");
            }
            result.close();
            getHashedPWStatement.close();
            
            String sessionKey = BCrypt.hashpw(userName + System.currentTimeMillis(), BCrypt.gensalt());
            Timestamp sessionValidDate = new Timestamp(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(SESSION_VALID_DAYS));
            String sessionClient = request.getHeader("User-Agent") == null ? "NO USER-AGENT PROVIDED" : request.getHeader("User-Agent");
            
            PreparedStatement newSession = SQLManager.getConn("RHCareerFairLayout").prepareStatement("INSERT INTO Sessions " + "VALUES(?, ?, ?, ?);");
            
            newSession.setString(1, userName);
            newSession.setString(2, sessionKey);
            newSession.setTimestamp(3, sessionValidDate);
            newSession.setString(4, sessionClient);
            
            newSession.executeUpdate();
            
            newSession.close();
            
            SuccessResponse response = new SuccessResponse();
            
            response.addToReturnData("token", sessionKey);
            response.addCookie("authUser", userName);
            response.addCookie("authToken", sessionKey);
            
            return response;
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
            
            PreparedStatement getAuthToken =
                    SQLManager.getConn("RHCareerFairLayout").prepareStatement(
                            "SELECT sessionKey, sessionValidDate FROM Sessions WHERE username = ? AND sessionClient = ?;");
            getAuthToken.setString(1, userName);
            getAuthToken.setString(2, request.getHeader("User-Agent") == null ? "NO USER-AGENT PROVIDED" : request.getHeader("User-Agent"));
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
            
            PreparedStatement getAuthToken =
                    SQLManager.getConn("RHCareerFairLayout").prepareStatement(
                            "UPDATE Sessions SET sessionValidDate = ? WHERE username = ? AND sessionKey = ? AND sessionClient = ?;");
            getAuthToken.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            getAuthToken.setString(2, userName);
            getAuthToken.setString(3, token);
            getAuthToken.setString(4, request.getHeader("User-Agent") == null ? "NO USER-AGENT PROVIDED" : request.getHeader("User-Agent"));
            getAuthToken.executeUpdate();
            
            return new SuccessResponse();
        } catch (Exception e) {
            ServletLog.logEvent(e);
            
            return new FailResponse(e);
        }
    }
}
