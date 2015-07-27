package cf.obsessiveorange.rhcareerfairlayout.data.models.wrappers;

import android.content.ContentValues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import cf.obsessiveorange.rhcareerfairlayout.data.models.Term;

/**
 * Created by Benedict on 7/14/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataWrapper {

    private final CategoryMap categoryMap;
    private final CompanyMap companyMap;
    private final TableMappingArray tableMappingList;
    private final CompanyCategoryMap companyCategoryMap;
    private final Term term;

    public DataWrapper(@JsonProperty("categoryMap") CategoryMap categoryMap,
                       @JsonProperty("companyMap") CompanyMap companyMap,
                       @JsonProperty("tableMappingList") TableMappingArray tableMappingList,
                       @JsonProperty("companyCategoryMap") CompanyCategoryMap companyCategoryMap,
                       @JsonProperty("term") Term term) {

        this.categoryMap = categoryMap;
        this.companyMap = companyMap;
        this.tableMappingList = tableMappingList;
        this.companyCategoryMap = companyCategoryMap;
        this.term = term;
    }

    public CategoryMap getCategoryMap() {
        return categoryMap;
    }

    public CompanyMap getCompanyMap() {
        return companyMap;
    }

    public TableMappingArray getTableMappingList() {
        return tableMappingList;
    }

    public CompanyCategoryMap getCompanyCategoryMap() {
        return companyCategoryMap;
    }

    public Term getTerm() {
        return term;
    }

}
