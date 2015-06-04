package managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SQLManager {
    
    private static String                  dbHost      = System.getenv("OPENSHIFT_MYSQL_DB_HOST");
    private static String                  dbPort      = System.getenv("OPENSHIFT_MYSQL_DB_PORT");
    private static String                  dbUserName  = System.getenv("OPENSHIFT_MYSQL_DB_USERNAME");
    private static String                  dbPassword  = System.getenv("OPENSHIFT_MYSQL_DB_PASSWORD");
    
    private static Map<String, Connection> connections = new HashMap<String, Connection>();
    
    public static Connection getConn() throws ClassNotFoundException, SQLException {
    
        return getConn("RHCareerFairLayout");
    }
    
    public static Connection getConn(String dbName) throws SQLException, ClassNotFoundException {
    
        if (connections.get(dbName) == null || connections.get(dbName).isClosed()) {
            setupConnection(dbName);
        }
        
        return connections.get(dbName);
    }
    
    private static void setupConnection(String dbName) throws ClassNotFoundException, SQLException {
    
        Class.forName("com.mysql.jdbc.Driver");
        
        if (System.getenv("OPENSHIFT_MYSQL_DB_HOST") == null) {
            System.out.println("Switching to localhost");
            
            dbHost = "localhost";
            dbPort = "3306";
            dbUserName = "root";
            dbPassword = "password";
        }
        
        connections.put(dbName, DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName, dbUserName, dbPassword));
    }
}