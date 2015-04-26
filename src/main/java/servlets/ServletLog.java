package servlets;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import com.google.gson.Gson;

public class ServletLog {
    
    private static final Queue<LogEvent> log = new LinkedList<LogEvent>();
    
    public static void logEvent(LogEvent event) {
    
        if (log.size() >= 5000) {
            log.poll();
        }
        
        log.add(event);
    }
    
    public static String getLogJson() {
    
        return new Gson().toJson(log);
    }
    
    public static class LogEvent {
        
        private final HashMap<String, Object> details   = new HashMap<String, Object>();
        private final long                    timestamp = System.currentTimeMillis();
        
        public void setDetail(String key, Object value) {
        
            details.put(key, value);
        }
        
        public long getTimestamp() {
        
            return timestamp;
        }
    }
}
