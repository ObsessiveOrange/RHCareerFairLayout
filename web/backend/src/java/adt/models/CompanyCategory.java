package adt.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

public class CompanyCategory {

    protected Long companyId;
    protected HashSet<Long> categories;

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

    public CompanyCategory(ResultSet rs) throws SQLException {

	this(rs.getLong("companyId"), rs.getLong("categoryId"));
    }

    public CompanyCategory(Long companyId, Long categoryId) {

	this.companyId = companyId;
	this.categories = new HashSet<Long>();
	this.categories.add(categoryId);
    }

    public Long getCompanyId() {

	return companyId;
    }

    public void setCompanyId(Long companyId) {

	this.companyId = companyId;
    }

    public HashSet<Long> getCategories() {

	return categories;
    }

    public void addCategoryId(Long categoryId) {

	this.categories.add(categoryId);
    }

    public void addCategories(HashSet<Long> categories) {

	this.categories.addAll(categories);
    }

}
