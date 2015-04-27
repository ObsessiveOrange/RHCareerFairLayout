package adt;

import misc.DataTable;

public class Sheet {
    
    private final String    name;
    private final DataTable data;
    
    public Sheet(String name, DataTable data) {
    
        this.name = name;
        this.data = data;
    }
    
    /**
     * @return the name
     */
    public String getName() {
    
        return name;
    }
    
    /**
     * @return the data
     */
    public DataTable getData() {
    
        return data;
    }
}
