package cf.obsessiveorange.rhcareerfairlayout.data.models.wrappers;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

import cf.obsessiveorange.rhcareerfairlayout.data.models.TableMapping;


public class TableMappingArray extends ArrayList<TableMapping> {

    /**
     *
     */
    private static final long serialVersionUID = -4617007484637352031L;

    public TableMappingArray(){
        super();
    }

    public TableMappingArray(Cursor cursor) {
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            this.add(new TableMapping(cursor));
        }
    }

    public ContentValues[] getContentValues(){
        ContentValues[] rows = new ContentValues[size()];

        int i = 0;
        for (TableMapping tableMapping : this){
            rows[i] = tableMapping.toContentValues();
            i++;
        }

        return rows;
    }
}
