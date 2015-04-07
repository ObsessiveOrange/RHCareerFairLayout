package adt;

import java.util.ArrayList;

import servlets.data.DataServlet;

public class Category extends Item implements Comparable<Category> {
    
    /**
     * 
     */
    private static final long  serialVersionUID = -7441122606014580042L;
    private String             title;
    private String             type;
    private ArrayList<Integer> entries;
    
    public Category(String title, String type) {
    
        super(DataServlet.dataVars.getNextCategoryID());
        
        this.title = title;
        this.type = type;
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
