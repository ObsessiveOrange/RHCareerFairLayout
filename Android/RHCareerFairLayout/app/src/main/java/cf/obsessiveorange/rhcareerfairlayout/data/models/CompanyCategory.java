package cf.obsessiveorange.rhcareerfairlayout.data.models;

import android.content.ContentValues;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import cf.obsessiveorange.rhcareerfairlayout.data.DBAdapter;

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

    public CompanyCategory(@JsonProperty("companyId") Long companyId,
                           @JsonProperty("categoryId") Long categoryId) {

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

    public List<ContentValues> toContentValues() {
        List<ContentValues> contentValuesList = new ArrayList<ContentValues>();

        for(Long category : categories){
            ContentValues row = new ContentValues();

            row.put(DBAdapter.KEY_COMPANY_ID, this.getCompanyId());
            row.put(DBAdapter.KEY_CATEGORY_ID, category);

            contentValuesList.add(row);
        }

        return contentValuesList;
    }

}
