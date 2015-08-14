package managers;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;

public class SQLManager {

    private static BasicDataSource dataSource;

    private static String dbHost = System.getenv("OPENSHIFT_MYSQL_DB_HOST");
    private static String dbPort = System.getenv("OPENSHIFT_MYSQL_DB_PORT");
    private static String dbUserName = System.getenv("OPENSHIFT_MYSQL_DB_USERNAME");
    private static String dbPassword = System.getenv("OPENSHIFT_MYSQL_DB_PASSWORD");
    private static String dbName = "RHCareerFairLayout";

    static {

	if (System.getenv("OPENSHIFT_MYSQL_DB_HOST") == null) {
	    System.out.println("Switching to localhost");

	    dbHost = "localhost";
	    dbPort = "3306";
	    dbUserName = "root";
	    dbPassword = "password";
	}

	dataSource = new BasicDataSource();
	dataSource.setDriverClassName("com.mysql.jdbc.Driver");
	dataSource.setUrl("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName);
	dataSource.setUsername(dbUserName);
	dataSource.setPassword(dbPassword);
	dataSource.setInitialSize(10);
	dataSource.setMinIdle(10);
	dataSource.setMaxIdle(20);
	dataSource.setMaxTotal(100);
	dataSource.setValidationQuery("SELECT 1");
	dataSource.setTestOnBorrow(true);
	dataSource.setTestOnReturn(true);
	dataSource.setTestWhileIdle(true);
	dataSource.setMaxWaitMillis(10000);
	dataSource.setRemoveAbandonedOnBorrow(true);
	dataSource.setRemoveAbandonedOnMaintenance(true);
	dataSource.setRemoveAbandonedTimeout(30);
	dataSource.setMinEvictableIdleTimeMillis(30000);
	dataSource.setTimeBetweenEvictionRunsMillis(30000);
    }

    public static Connection getConn() throws ClassNotFoundException, SQLException {

	return dataSource.getConnection();
    }
}