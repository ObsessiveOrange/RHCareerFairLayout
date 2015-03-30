package servlets;

import java.util.HashMap;

import com.google.gson.Gson;

public class SystemVars {
    
    private static final String dbHost     = System.getenv("OPENSHIFT_MYSQL_DB_HOST");
    private static final String dbPort     = System.getenv("OPENSHIFT_MYSQL_DB_PORT");
    private static final String dbUserName = System.getenv("OPENSHIFT_MYSQL_DB_USERNAME");
    private static final String dbPassword = System.getenv("OPENSHIFT_MYSQL_DB_PASSWORD");
    private static final String gearName   = System.getenv("OPENSHIFT_GEAR_NAME");
    private static final String term       = "Spring1415";
    
    /**
     * @return the dbhost
     */
    public static String getDbhost() {
    
        return dbHost;
    }
    
    /**
     * @return the dbport
     */
    public static String getDbport() {
    
        return dbPort;
    }
    
    /**
     * @return the dbusername
     */
    public static String getDbusername() {
    
        return dbUserName;
    }
    
    /**
     * @return the dbpassword
     */
    public static String getDbpassword() {
    
        return dbPassword;
    }
    
    /**
     * @return the gearname
     */
    public static String getGearname() {
    
        return gearName;
    }
    
    /**
     * @return the term
     */
    public static String getTerm() {
    
        return term;
    }
    
    public static String failString(String error) {
    
        HashMap<String, Object> returnMap = new HashMap<String, Object>();
        
        returnMap.put("success", 0);
        if (error != null) {
            returnMap.put("error", error);
        }
        returnMap.put("timestamp", System.currentTimeMillis());
        
        return new Gson().toJson(returnMap);
    }
    
    public static String successString(String message) {
    
        HashMap<String, Object> returnMap = new HashMap<String, Object>();
        
        returnMap.put("success", 1);
        if (message != null) {
            returnMap.put("message", message);
        }
        returnMap.put("timestamp", System.currentTimeMillis());
        
        return new Gson().toJson(returnMap);
    }
}
