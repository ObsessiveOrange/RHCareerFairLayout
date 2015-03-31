package managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import servlets.SystemVars;

public class SQLManager {
    
    private static Map<String, Connection> connections = new HashMap<String, Connection>();
    
    public static Connection getConn(String dbName) {
    
        if (connections.get(dbName) == null) {
            setupConnection(dbName);
        }
        
        return connections.get(dbName);
    }
    
    private static boolean setupConnection(String dbName) {
    
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            connections.put(dbName,
                    DriverManager.getConnection("jdbc:mysql://" + SystemVars.getDbhost() + ":" + SystemVars.getDbport() + "/" + dbName,
                            SystemVars.getDbusername(), SystemVars.getDbpassword()));
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}
