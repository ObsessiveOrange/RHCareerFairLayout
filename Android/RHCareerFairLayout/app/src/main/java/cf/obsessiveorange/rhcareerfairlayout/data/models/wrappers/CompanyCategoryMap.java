package cf.obsessiveorange.rhcareerfairlayout.data.models.wrappers;

import android.content.ContentValues;

import java.util.ArrayList;
import java.util.HashMap;

import cf.obsessiveorange.rhcareerfairlayout.data.models.CompanyCategory;

public class CompanyCategoryMap extends HashMap<Long, CompanyCategory> {

    /**
     * 
     */
    private static final long serialVersionUID = -4617007484637352031L;

    public ContentValues[] getContentValues(){
        ArrayList<ContentValues> rows = new ArrayList<ContentValues>();

        for (CompanyCategory companyCategory : values()){
            rows.addAll(companyCategory.toContentValues());
        }

        return rows.toArray(new ContentValues[0]);
    }
}
