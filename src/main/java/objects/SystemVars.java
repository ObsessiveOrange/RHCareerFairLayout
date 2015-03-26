package objects;

public class SystemVars {
    
    private int categoryIDCounter = 1;
    private int entryIDCounter    = 100;
    
    public synchronized int getNextCategoryID() {
    
        return categoryIDCounter++;
    }
    
    public synchronized int getNextEntryID() {
    
        return entryIDCounter++;
    }
}
