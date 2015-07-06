package adt;

import java.util.ArrayList;
import java.util.List;

public class Company extends Entry implements Comparable<Company> {

    protected String name;
    protected List<Long> categories;
    protected String description;
    protected Long tableId;

    /**
     * Constructor for reconstructing from SQL queries
     * 
     * @param id
     *            The ID of the company
     * @param name
     *            The name of the company
     * @param description
     *            A description of the company (Can be null)
     * @param tableNumber
     *            The table the company will be at.
     */
    public Company(Long id, String name, String description, Long tableId) {

	this(id, name, new ArrayList<Long>(), description, tableId);
    }

    public Company(Long id, String name, List<Long> categories, String description, Long tableId) {

	super(id);
	this.name = name;
	this.categories = categories;
	this.description = description;
	this.tableId = tableId;
    }

    public String getName() {

	return name;
    }

    public void setName(String name) {

	this.name = name;
    }

    public List<Long> getCategories() {

	return categories;
    }

    public void setCategories(List<Long> categories) {

	this.categories = categories;
    }

    public String getDescription() {

	return description;
    }

    public void setDescription(String description) {

	this.description = description;
    }

    public Long getTableId() {

	return tableId;
    }

    public void setTableId(Long tableId) {

	this.tableId = tableId;
    }

    @Override
    public int compareTo(Company other) {

	return name.compareTo(other.getName());
    }

}
