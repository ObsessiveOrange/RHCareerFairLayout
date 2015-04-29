package adt;


public class Category extends Item implements Comparable<Category> {
    
    /**
     * 
     */
    private static final long serialVersionUID = -7441122606014580042L;
    private String            title;
    private String            type;
    
    public Category(Integer id, String title, String type) {
    
        super(id);
        
        this.title = title;
        this.type = type;
        
    }
    
    public Category(String title, String type) {
    
        super(DataVars.getNextCategoryID());
        
        this.title = title;
        this.type = type;
        
    }
    
    public String getTitle() {
    
        return title;
    }
    
    public String getType() {
    
        return type;
    }
    
    @Override
    public int compareTo(Category other) {
    
        return title.compareTo(other.getTitle());
    }
}
