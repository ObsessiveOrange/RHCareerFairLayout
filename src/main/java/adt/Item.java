package adt;

import java.io.Serializable;

import com.google.gson.Gson;

public class Item implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 8775375254185524748L;
    private static final Gson gson             = new Gson();
    
    private long              id;
    private long              lastUpdateTime;
    
    public enum ItemType {
        category,
        entry
    }
    
    /************************************************* Instance methods ***********************************************/
    public Item(long id) {
    
        this.id = id;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    public int getID() {
    
        return (int) id;
    }
    
    public long getLastUpdateTime() {
    
        return lastUpdateTime;
    }
    
    public void wasEdited() {
    
        lastUpdateTime = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
    
        return gson.toJson(this);
    }
}
