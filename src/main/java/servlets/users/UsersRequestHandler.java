package servlets.users;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;

import managers.SQLManager;
import misc.BCrypt;
import servlets.ServletLog;
import adt.Response;
import adt.Response.FailResponse;
import adt.Response.SuccessResponse;

public class UsersRequestHandler {
    
    public static Response handleRegisterUserRequest(HttpServletRequest request) {
    
        try {
            PreparedStatement statement;
            
            String user = request.getHeader("authUser");
            String pass = request.getHeader("authPass");
            
            // check to make sure username does not already exist
            statement = SQLManager.getConn("Users").prepareStatement("SELECT COUNT(id) FROM Users WHERE username = '" + user + "';");
            ResultSet rs = statement.executeQuery();
            rs.next();
            if (rs.getInt(1) != 0) {
                return new FailResponse("Username already exists");
            }
            rs.close();
            statement.close();
            
            // Permissions levels:
            // 1 - Users (Edit saved companies, visit list)
            // 10 - Admin (Edit user permssions, edit company/category list)
            // Always add as users, require admin access to elevate
            statement = SQLManager.getConn("Users").prepareStatement("INSERT INTO Users (username, hashedPw, permissions) VALUES (?, ?, 1);");
            statement.setString(1, user);
            statement.setString(2, BCrypt.hashpw(pass, BCrypt.gensalt()));
            Integer insertResult = statement.executeUpdate();
            if (insertResult == 0) {
                return new FailResponse("Username already exists");
            }
            rs.close();
            statement.close();
            
            return new SuccessResponse("Rows changed: " + insertResult);
        } catch (Exception e) {
            ServletLog.logEvent(e);
            
            return new FailResponse(e);
        }
        
    }
    
    public static Response handleLoginRequest(HttpServletRequest request) {
    
        try {
            
            String user = request.getHeader("authUser");
            String pass = request.getHeader("authPass");
            
            PreparedStatement prepStatement =
                    SQLManager.getConn("Users").prepareStatement("SELECT hashedPw FROM Users WHERE username = '" + user + "';");
            
            ResultSet rs = prepStatement.executeQuery();
            rs.next();
            
            if (!BCrypt.checkpw(pass, rs.getString("hashedPw"))) {
                return new FailResponse("Username and password do not match");
            }
            
            return new SuccessResponse("success");
        } catch (Exception e) {
            ServletLog.logEvent(e);
            
            return new FailResponse(e);
        }
        
    }
    
}
