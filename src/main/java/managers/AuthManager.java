package managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import misc.BCrypt;
import servlets.ServletLog;
import servlets.ServletLog.LogEvent;
import adt.Response;
import adt.Response.FailResponse;
import adt.Response.SuccessResponse;

public class AuthManager {
    
    private static final int         SESSION_VALID_DAYS   = 15;
    
    private static PreparedStatement getHashedPWStatement = null;
    private static PreparedStatement check                = null;
    private static PreparedStatement newSession           = null;
    private static PreparedStatement getAuthToken         = null;
    
    private static boolean           isSetup              = false;
    
    public static void setupAuthManager() {
    
        if (!isSetup) {
            
            try {
                getHashedPWStatement = SQLManager.getConn("Users").prepareStatement("SELECT hashedPw FROM Users WHERE username = ?;");
                check = SQLManager.getConn("Users").prepareStatement("SELECT COUNT(username) FROM Users WHERE username = ?;");
                newSession =
                        SQLManager.getConn("Users").prepareStatement("INSERT INTO Sessions "
                                + "VALUES(?, ?, ?, ?);");
                getAuthToken =
                        SQLManager.getConn("Users").prepareStatement(
                                "SELECT sessionKey, sessionValidDate FROM Sessions WHERE username = ? AND sessionClient = ?;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static Response addUser(HttpServletRequest request) {
    
        try {
            setupAuthManager();
            
            String userName = request.getHeader("authUser");
            String password = request.getHeader("authPass");
            
            // check to make sure username does not already exist
            check.setString(1, userName);
            ResultSet users = check.executeQuery();
            users.next();
            if (users.getInt(1) != 0) {
                return new FailResponse("Username already exists");
            }
            
            // Permissions levels:
            // 1 - Users (Edit saved companies, visit list)
            // 10 - Admin (Edit user permssions, edit company/category list)
            // Always add as users, require admin access to elevate
            PreparedStatement statement =
                    SQLManager.getConn("Users").prepareStatement("INSERT INTO Users (username, hashedPw, permissions) VALUES (?, ?, 1);");
            statement.setString(1, userName);
            statement.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
            Integer insertResult = statement.executeUpdate();
            
            return new SuccessResponse("Rows changed: " + insertResult);
        } catch (SQLException e) {
            e.printStackTrace();
            return new FailResponse(e);
        }
    }
    
    public static Response authenticateUser(HttpServletRequest request) {
    
        try {
            setupAuthManager();
            
            // Response checkTokenResponse;
            // if ((checkTokenResponse = AuthManager.checkToken(request)).success) {
            // return checkTokenResponse;
            // }
            
            String userName = request.getHeader("authUser");
            String password = request.getHeader("authPass");
            
            if (userName == null || password == null) {
                return new FailResponse("Invalid Username/Password Combination");
            }
            
            getHashedPWStatement.setString(1, userName);
            
            ResultSet result = getHashedPWStatement.executeQuery();
            result.next();
            
            if (!BCrypt.checkpw(password, result.getString("hashedPw"))) {
                return new FailResponse("Invalid Username/Password Combination");
            }
            
            String sessionKey = BCrypt.hashpw(userName + System.currentTimeMillis(), BCrypt.gensalt());
            Timestamp sessionValidDate = new Timestamp(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(SESSION_VALID_DAYS));
            String sessionClient = request.getHeader("User-Agent") == null ? "NO USER-AGENT PROVIDED" : request.getHeader("User-Agent");
            
            newSession.setString(1, userName);
            newSession.setString(2, sessionKey);
            newSession.setTimestamp(3, sessionValidDate);
            newSession.setString(4, sessionClient);
            
            newSession.executeUpdate();
            
            SuccessResponse response = new SuccessResponse();
            
            response.addToReturnData("token", sessionKey);
            response.setAuthCookie(sessionKey);
            
            return response;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new FailResponse(e);
        }
    }
    
    public static Response checkToken(HttpServletRequest request) {
    
        try {
            setupAuthManager();
            
            String userName = request.getHeader("authUser");
            String token = null;
            
            if (userName == null) {
                return new FailResponse("Username not provided");
            }
            if (request.getCookies() == null) {
                return new FailResponse("No token provided");
            }
            
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals("authToken")) {
                    token = c.getValue();
                }
            }
            
            if (token == null && request.getHeader("authUser") != null) {
                token = request.getHeader("authUser");
            }
            
            if (token == null) {
                return new FailResponse("No token provided");
            }
            
            getAuthToken.setString(1, userName);
            getAuthToken.setString(2, request.getHeader("User-Agent") == null ? "NO USER-AGENT PROVIDED" : request.getHeader("User-Agent"));
            ResultSet result = getAuthToken.executeQuery();
            
            LogEvent authEvent = new LogEvent();
            authEvent.setDetail("authUser", userName);
            authEvent.setDetail("authToken", token);
            
            boolean hasNextResult;
            while (hasNextResult = result.next()) {
                authEvent.setDetail("dbToken", result.getString("sessionKey"));
                authEvent.setDetail("dbValidDate", result.getTimestamp("sessionValidDate"));
                
                if (token.equals(result.getString("sessionKey"))
                        && result.getTimestamp("sessionValidDate").after(new Timestamp(System.currentTimeMillis()))) {
                    break;
                }
            }
            if (!hasNextResult) {
                return new FailResponse("Invalid Token");
            }
            
            result.close();
            
            ServletLog.logEvent(authEvent);
            return new SuccessResponse();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new FailResponse(e);
        }
    }
}
