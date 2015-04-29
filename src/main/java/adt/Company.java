package adt;

import java.util.ArrayList;
import java.util.List;

public class Company extends Item implements Comparable<Company> {
    
    /**
     * 
     */
    private static final long serialVersionUID = 418100621969341337L;
    private String            name;
    private List<Integer>     categories;
    private String            description;
    private Integer           tableNumber;
    
    /**
     * Constructor for reconstructing from SQL queries
     * 
     * @param id The ID of the company
     * @param name The name of the company
     * @param description A description of the company (Can be null)
     * @param tableNumber The table the company will be at.
     */
    public Company(Integer id, String name, String description, Integer tableNumber) {
    
        super(id);
        this.name = name;
        this.categories = new ArrayList<Integer>();
        this.description = description;
        this.tableNumber = tableNumber;
    }
    
    public Company(String name, List<Integer> categories, String description, Integer tableNumber) {
    
        super(DataVars.getNextEntryID());
        this.name = name;
        this.categories = categories;
        this.description = description;
        this.tableNumber = tableNumber;
    }
    
    public String getName() {
    
        return name;
    }
    
    public List<Integer> getCategories() {
    
        return categories;
    }
    
    public Integer getTableNumber() {
    
        return tableNumber;
    }
    
    public String getDescription() {
    
        return description;
    }
    
    @Override
    public int compareTo(Company other) {
    
        return name.compareTo(other.getName());
    }
    
}
