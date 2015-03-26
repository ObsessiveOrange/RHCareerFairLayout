package objects;

import java.util.ArrayList;

import main.RHCareerFairLayout;

public class Category extends Item implements Comparable<Category> {
    
    /**
     * 
     */
    private static final long  serialVersionUID = -7441122606014580042L;
    private String             title;
    private ArrayList<Integer> entries;
    
    public Category() {
    
        super(0);
    }
    
    public Category(String title, String type) {
    
        super(RHCareerFairLayout.dataVars.getNextCategoryID());
        
        this.title = title;
        this.entries = new ArrayList<Integer>();
        
    }
    
    public String getTitle() {
    
        return title;
    }
    
    public ArrayList<Integer> getEntries() {
    
        return entries;
    }
    
    @Override
    public int compareTo(Category other) {
    
        return title.compareTo(other.getTitle());
    }
}
