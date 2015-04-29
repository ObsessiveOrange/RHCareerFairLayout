package adt;

public class Category extends Item implements Comparable<Category> {
    
    /**
     * 
     */
    private static final long serialVersionUID = -7441122606014580042L;
    private String            name;
    private String            type;
    
    public Category(Integer id, String name, String type) {
    
        super(id);
        
        this.name = name;
        this.type = type;
        
    }
    
    public Category(String name, String type) {
    
        super(DataVars.getNextCategoryID());
        
        this.name = name;
        this.type = type;
        
    }
    
    public String getName() {
    
        return name;
    }
    
    public String getType() {
    
        return type;
    }
    
    @Override
    public int compareTo(Category other) {
    
        return name.compareTo(other.getName());
    }
}
