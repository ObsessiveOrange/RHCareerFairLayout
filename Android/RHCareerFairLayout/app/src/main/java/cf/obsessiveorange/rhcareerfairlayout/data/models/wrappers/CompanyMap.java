package cf.obsessiveorange.rhcareerfairlayout.data.models.wrappers;

import android.content.ContentValues;

import java.util.HashMap;

import cf.obsessiveorange.rhcareerfairlayout.data.DBAdapter;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Company;


public class CompanyMap extends HashMap<Long, Company> {

    /**
     * 
     */
    private static final long serialVersionUID = -4617007484637352031L;


    public ContentValues[] getContentValues(){
        ContentValues[] rows = new ContentValues[size()];

        int i = 0;
        for (Company company : values()){
            rows[i] = company.toContentValues();
            i++;
        }

        return rows;
    }

    public ContentValues[] getInitialSelectionContentValues(){
        ContentValues[] rows = new ContentValues[size()];

        int i = 0;
        for (Company company : values()){

            ContentValues row = new ContentValues();

            row.put(DBAdapter.KEY_COMPANY_ID, company.getId());
            row.put(DBAdapter.KEY_SELECTED, false);

            rows[i] = row;
            i++;
        }

        return rows;
    }
}
