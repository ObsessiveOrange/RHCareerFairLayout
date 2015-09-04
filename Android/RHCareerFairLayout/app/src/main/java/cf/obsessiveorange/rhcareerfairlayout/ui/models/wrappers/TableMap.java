package cf.obsessiveorange.rhcareerfairlayout.ui.models.wrappers;

import android.database.Cursor;

import java.util.HashMap;

import cf.obsessiveorange.rhcareerfairlayout.ui.models.Table;

/**
 * Created by Benedict on 7/22/2015.
 */
public class TableMap extends HashMap<Long, Table> {

    public TableMap(Cursor cursor) {
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Table table = new Table(cursor);
            this.put(table.getId(), table);
        }
    }
}
