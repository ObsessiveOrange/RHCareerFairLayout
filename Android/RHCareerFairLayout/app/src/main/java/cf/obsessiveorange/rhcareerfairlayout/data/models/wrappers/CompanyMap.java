package cf.obsessiveorange.rhcareerfairlayout.data.models.wrappers;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.HashMap;

import cf.obsessiveorange.rhcareerfairlayout.data.DBAdapter;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Company;


public class CompanyMap extends HashMap<Long, Company> {

    /**
     * 
     */
    private static final long serialVersionUID = -4617007484637352031L;

    public CompanyMap(){
        super();
    }

    public CompanyMap(Cursor cursor) {
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            Company company = new Company(cursor);
            this.put(company.getId(), company);
        }

        cursor.close();
    }

    public ContentValues[] getContentValues(){
        ContentValues[] rows = new ContentValues[size()];

        int i = 0;
        for (Company company : values()){
            rows[i] = company.toContentValues();
            i++;
        }

        return rows;
    }

    public ContentValues[] getSelectionContentValues(boolean selected){
        ContentValues[] rows = new ContentValues[size()];

        int i = 0;
        for (Company company : values()){

            ContentValues row = new ContentValues();

            row.put(DBAdapter.KEY_COMPANY_ID, company.getId());
            row.put(DBAdapter.KEY_SELECTED, selected);

            rows[i] = row;
            i++;
        }

        return rows;
    }
}
