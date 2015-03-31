package managers;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import misc.BCrypt;
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
                check = SQLManager.getConn("Users").prepareStatement("SELECT COUNT(id) FROM Users WHERE username = ?;");
                newSession =
                        SQLManager.getConn("Users").prepareStatement("INSERT INTO Sessions"
                                + "VALUES(?, ?, ?, ?");
                getAuthToken =
                        SQLManager.getConn("Users").prepareStatement("SELECT sessionKey FROM Sessions WHERE username = ? AND sessionClient = ?;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static Response addUser(String userName, String password) {
    
        try {
            setupAuthManager();
            
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
            return new FailResponse(e.toString());
        }
    }
    
    public static Response authenticateUser(HttpServletRequest request) {
    
        try {
            setupAuthManager();
            
            String userName = request.getHeader("authUser");
            String password = request.getHeader("authPass");
            
            getHashedPWStatement.setString(1, userName);
            
            ResultSet result = getHashedPWStatement.executeQuery();
            result.next();
            
            if (!BCrypt.checkpw(password, result.getString("hashedPw"))) {
                return new FailResponse("Invalid Username/Password Combination");
            }
            
            String sessionKey = BCrypt.hashpw(userName + System.currentTimeMillis(), BCrypt.gensalt());
            Date sessionValidDate = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(SESSION_VALID_DAYS));
            String sessionClient = request.getHeader("User-Agent") == null ? "NO USER-AGENT PROVIDED" : request.getHeader("User-Agent");
            
            newSession.setString(1, userName);
            newSession.setString(2, sessionKey);
            newSession.setDate(3, sessionValidDate);
            newSession.setString(4, sessionClient);
            
            Integer resultInt = newSession.executeUpdate();
            
            SuccessResponse response = new SuccessResponse();
            
            response.addToReturnData("token", sessionKey);
            response.addToReturnData("[DEBUG] dbResult", resultInt);
            
            return response;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new FailResponse(e.toString());
        }
    }
    
    public static Response checkToken(HttpServletRequest request) {
    
        try {
            setupAuthManager();
            
            String userName = request.getHeader("authUser");
            String token = request.getHeader("authToken");
            
            getAuthToken.setString(1, userName);
            getAuthToken.setString(2, request.getHeader("User-Agent") == null ? "NO USER-AGENT PROVIDED" : request.getHeader("User-Agent"));
            ResultSet result = getHashedPWStatement.executeQuery();
            
            boolean hasNextResult;
            while (hasNextResult = result.next()) {
                if (token.equals(result.getString("sessionKey"))) {
                    break;
                }
            }
            if (!hasNextResult) {
                return new FailResponse("Invalid Username/Password Combination");
            }
            
            String sessionKey = BCrypt.hashpw(userName + System.currentTimeMillis(), BCrypt.gensalt());
            Date sessionValidDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(SESSION_VALID_DAYS));
            String sessionClient = request.getHeader("User-Agent") == null ? "NO USER-AGENT PROVIDED" : request.getHeader("User-Agent");
            String sessionIP = request.getRemoteAddr();
            
            newSession.setString(1, userName);
            newSession.setString(2, sessionKey);
            newSession.setDate(3, sessionValidDate);
            newSession.setString(4, sessionClient);
            
            Integer resultInt = newSession.executeUpdate();
            
            SuccessResponse response = new SuccessResponse();
            response.setAuthCookie(sessionKey);
            
            return response;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new FailResponse(e);
        }
    }
}
