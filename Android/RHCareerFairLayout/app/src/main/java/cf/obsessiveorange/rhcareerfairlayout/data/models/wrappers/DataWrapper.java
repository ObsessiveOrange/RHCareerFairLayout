package cf.obsessiveorange.rhcareerfairlayout.data.models.wrappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import cf.obsessiveorange.rhcareerfairlayout.data.models.Term;

/**
 * Created by Benedict on 7/14/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataWrapper {

    private final CategoryMap categoryMap;
    private final CompanyCategoryMap companyCategoryMap;
    private final CompanyMap companyMap;
    private final TableMappingArray tableMappingList;
    private final Term term;

    public DataWrapper(@JsonProperty("categoryMap") CategoryMap categoryMap,
                       @JsonProperty("companyCategoryMap") CompanyCategoryMap companyCategoryMap,
                       @JsonProperty("companyMap") CompanyMap companyMap,
                       @JsonProperty("tableMappingList") TableMappingArray tableMappingList,
                       @JsonProperty("term") Term term) {

        this.categoryMap = categoryMap;
        this.companyCategoryMap = companyCategoryMap;
        this.companyMap = companyMap;
        this.tableMappingList = tableMappingList;
        this.term = term;
    }

    public CategoryMap getCategoryMap() {
        return categoryMap;
    }

    public CompanyCategoryMap getCompanyCategoryMap() {
        return companyCategoryMap;
    }

    public CompanyMap getCompanyMap() {
        return companyMap;
    }

    public TableMappingArray getTableMappingList() {
        return tableMappingList;
    }

    public Term getTerm() {
        return term;
    }

}
