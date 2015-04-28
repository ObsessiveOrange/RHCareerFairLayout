package adt;

import java.util.List;

import servlets.data.DataServlet;

public class Company extends Item implements Comparable<Company> {
    
    /**
     * 
     */
    private static final long serialVersionUID = 418100621969341337L;
    private String            name;
    private List<Integer>     categories;
    private String            description;
    private Integer           tableNumber;
    
    public Company() {
    
        super(0);
    }
    
    public Company(String name, List<Integer> categories, String description, Integer tableNumber) {
    
        super(DataVars.getNextEntryID());
        this.name = name;
        this.categories = categories;
        this.description = description;
        this.tableNumber = tableNumber;
        
        // add to all categories in list
        for (Integer id : categories) {
            if (id == 0) {
                continue;
            }
            Category category = DataServlet.categoryMap.get(id);
            category.getEntries().add(this.getID());
        }
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
