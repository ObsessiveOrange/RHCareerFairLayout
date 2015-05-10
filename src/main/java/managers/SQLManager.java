package managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

public class SQLManager {
    
    private static final String            dbHost      = System.getenv("OPENSHIFT_MYSQL_DB_HOST");
    private static final String            dbPort      = System.getenv("OPENSHIFT_MYSQL_DB_PORT");
    private static final String            dbUserName  = System.getenv("OPENSHIFT_MYSQL_DB_USERNAME");
    private static final String            dbPassword  = System.getenv("OPENSHIFT_MYSQL_DB_PASSWORD");
    
    private static Map<String, DataSource> dataSources = new HashMap<String, DataSource>();
    
    public static Connection getConn() throws ClassNotFoundException, SQLException {
    
        return getConn("RHCareerFairLayout");
    }
    
    public static Connection getConn(String dbName) throws ClassNotFoundException, SQLException {
    
        if (dataSources.get(dbName) == null) {
            setupConnection(dbName);
        }
        
        return dataSources.get(dbName).getConnection();
    }
    
    private static void setupConnection(String dbName) throws ClassNotFoundException, SQLException {
    
        PoolProperties p = new PoolProperties();
        p.setUrl("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName);
        p.setDriverClassName("com.mysql.jdbc.Driver");
        p.setUsername(dbUserName);
        p.setPassword(dbPassword);
        p.setJmxEnabled(true);
        p.setTestWhileIdle(false);
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");
        p.setTestOnReturn(false);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(100);
        p.setInitialSize(1);
        p.setMaxWait(10000);
        p.setRemoveAbandonedTimeout(60);
        p.setMinEvictableIdleTimeMillis(30000);
        p.setMinIdle(10);
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        p.setJdbcInterceptors(
                "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" +
                        "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        DataSource datasource = new DataSource();
        datasource.setPoolProperties(p);
        //
        // BasicDataSource connectionPool;
        // String dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
        // connectionPool = new BasicDataSource();
        //
        // connectionPool.setDriverClassName("com.mysql.jdbc.Driver");
        // connectionPool.setUrl(dbUrl);
        // connectionPool.setUsername(dbUserName);
        // connectionPool.setPassword(dbPassword);
        // connectionPool.setInitialSize(1);
        
        dataSources.put(dbName,
                datasource);
    }
}
