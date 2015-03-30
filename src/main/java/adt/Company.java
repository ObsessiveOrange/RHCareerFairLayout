package adt;

import java.util.List;
import java.util.Map;

import servlets.data.DataServlet;

public class Company extends Item implements Comparable<Company> {
    
    /**
     * 
     */
    private static final long          serialVersionUID = 418100621969341337L;
    private String                     title;
    private Map<String, List<Integer>> categories;
    private String                     description;
    private Map<String, String>        parameters;
    
    public Company() {
    
        super(0);
    }
    
    public Company(String title, Map<String, List<Integer>> categories, String description, Map<String, String> parameters) {
    
        super(DataServlet.dataVars.getNextEntryID());
        this.title = title;
        this.categories = categories;
        this.description = description;
        this.parameters = parameters;
        
        // add to all categories in list
        for (List<Integer> categoryList : categories.values()) {
            for (Integer id : categoryList) {
                if (id == 0) {
                    continue;
                }
                Category category = DataServlet.categoryMap.get(id);
                category.getEntries().add(this.getID());
            }
        }
    }
    
    public String getTitle() {
    
        return title;
    }
    
    public Map<String, List<Integer>> getCategories() {
    
        return categories;
    }
    
    public String getDescription() {
    
        return description;
    }
    
    public Map<String, String> getParameters() {
    
        return parameters;
    }
    
    @Override
    public int compareTo(Company other) {
    
        return title.compareTo(other.getTitle());
    }
}
