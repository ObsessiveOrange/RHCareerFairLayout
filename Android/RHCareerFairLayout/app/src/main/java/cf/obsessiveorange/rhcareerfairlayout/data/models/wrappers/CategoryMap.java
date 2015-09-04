package cf.obsessiveorange.rhcareerfairlayout.data.models.wrappers;

import android.content.ContentValues;

import java.util.HashMap;

import cf.obsessiveorange.rhcareerfairlayout.data.managers.DBManager;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Category;


public class CategoryMap extends HashMap<Long, Category> {

    /**
     * 
     */
    private static final long serialVersionUID = -4617007484637352031L;


    public ContentValues[] getContentValues(){
        ContentValues[] rows = new ContentValues[size()];

        int i = 0;
        for (Category category : values()){
            rows[i] = category.toContentValues();
            i++;
        }

        return rows;
    }

    public ContentValues[] getSelectionContentValues(boolean selected){
        ContentValues[] rows = new ContentValues[size()];

        int i = 0;
        for (Category category : values()){

            ContentValues row = new ContentValues();

            row.put(DBManager.KEY_CATEGORY_ID, category.getId());
            row.put(DBManager.KEY_SELECTED, selected);

            rows[i] = row;
            i++;
        }

        return rows;
    }
}
