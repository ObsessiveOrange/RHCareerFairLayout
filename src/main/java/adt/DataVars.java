package adt;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DataVars {
    
    private static int                               categoryIDCounter = 1;
    private static int                               entryIDCounter    = 100;
    private static Map<String, Map<String, Integer>> idLookupTable     = new HashMap<String, Map<String, Integer>>();
    private static int                               year              = 0;
    private static String                            quarter           = "";
    
    public static synchronized int getNextCategoryID() {
    
        return categoryIDCounter++;
    }
    
    public static synchronized int getNextEntryID() {
    
        return entryIDCounter++;
    }
    
    public static void addToIDLookupTable(String title, String type, Integer id) {
    
        if (idLookupTable.get(type) == null) {
            idLookupTable.put(type, new HashMap<String, Integer>());
        }
        
        idLookupTable.get(type).put(title.toLowerCase().trim(), id);
    }
    
    public static Collection<String> getAllTypes() {
    
        return idLookupTable.keySet();
    }
    
    public static Collection<Integer> getAllOfType(String type) {
    
        return idLookupTable.get(type).values();
    }
    
    public static Integer getFromIDLookupTable(String type, String title) {
    
        return idLookupTable.get(type).get(title.toLowerCase().trim());
    }
    
    public static int getYear() {
    
        return year;
    }
    
    public static void setYear(int year) {
    
        DataVars.year = year;
    }
    
    public static String getQuarter() {
    
        return quarter;
    }
    
    public static void setQuarter(String quarter) {
    
        DataVars.quarter = quarter;
    }
}
