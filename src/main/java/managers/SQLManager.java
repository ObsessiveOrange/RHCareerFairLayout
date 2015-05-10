package managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;

import servlets.ServletLog;

public class SQLManager {
    
    private static final String                 dbHost      = System.getenv("OPENSHIFT_MYSQL_DB_HOST");
    private static final String                 dbPort      = System.getenv("OPENSHIFT_MYSQL_DB_PORT");
    private static final String                 dbUserName  = System.getenv("OPENSHIFT_MYSQL_DB_USERNAME");
    private static final String                 dbPassword  = System.getenv("OPENSHIFT_MYSQL_DB_PASSWORD");
    
    private static Map<String, BasicDataSource> connections = new HashMap<String, BasicDataSource>();
    
    public static Connection getConn() throws ClassNotFoundException, SQLException {
    
        return getConn("RHCareerFairLayout");
    }
    
    public static Connection getConn(String dbName) throws ClassNotFoundException, SQLException {
    
        try {
            if (connections.get(dbName) == null) {
                setupConnection(dbName);
            }
        } catch (SQLException e) {
            ServletLog.logEvent(e);
            
            throw e;
        }
        
        return connections.get(dbName).getConnection();
    }
    
    private static void setupConnection(String dbName) throws ClassNotFoundException, SQLException {
    
        BasicDataSource connectionPool;
        String dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
        connectionPool = new BasicDataSource();
        
        connectionPool.setUsername(dbUserName);
        connectionPool.setPassword(dbPassword);
        
        connectionPool.setDriverClassName("org.postgresql.Driver");
        connectionPool.setUrl(dbUrl);
        connectionPool.setInitialSize(1);
        
        connections.put(dbName,
                connectionPool);
    }
}
