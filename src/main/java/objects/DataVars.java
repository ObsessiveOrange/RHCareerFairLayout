package objects;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DataVars {
    
    private int                               categoryIDCounter = 1;
    private int                               entryIDCounter    = 100;
    private Map<String, Map<String, Integer>> idLookupTable     = new HashMap<String, Map<String, Integer>>();
    private int                               year              = 0;
    private String                            quarter           = "";
    
    public synchronized int getNextCategoryID() {
    
        return categoryIDCounter++;
    }
    
    public synchronized int getNextEntryID() {
    
        return entryIDCounter++;
    }
    
    public void addToIDLookupTable(String title, String type, Integer id) {
    
        if (idLookupTable.get(type) == null) {
            idLookupTable.put(type, new HashMap<String, Integer>());
        }
        
        idLookupTable.get(type).put(title.toLowerCase().trim(), id);
    }
    
    public Collection<Integer> getAllOfType(String type) {
    
        return idLookupTable.get(type).values();
    }
    
    public Integer getFromIDLookupTable(String type, String title) {
    
        return idLookupTable.get(type).get(title.toLowerCase().trim());
    }
    
    public int getYear() {
    
        return year;
    }
    
    public void setYear(int year) {
    
        this.year = year;
    }
    
    public String getQuarter() {
    
        return quarter;
    }
    
    public void setQuarter(String quarter) {
    
        this.quarter = quarter;
    }
}
