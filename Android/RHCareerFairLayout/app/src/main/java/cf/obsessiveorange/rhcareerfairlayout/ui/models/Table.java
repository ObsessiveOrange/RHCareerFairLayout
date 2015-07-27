package cf.obsessiveorange.rhcareerfairlayout.ui.models;

import android.database.Cursor;

import cf.obsessiveorange.rhcareerfairlayout.data.DBAdapter;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Entry;

/**
 * Created by Benedict on 7/22/2015.
 */
public class Table extends Entry {

    private Rectangle rectangle;
    private final int size;
    private final boolean selected;

    public Table(Cursor c) {

        super(c.getLong(c.getColumnIndexOrThrow(DBAdapter.KEY_ID)));

        this.size = c.getInt(c.getColumnIndexOrThrow(DBAdapter.KEY_SIZE));
        this.selected = c.getInt(c.getColumnIndexOrThrow(DBAdapter.KEY_SELECTED)) > 0;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    public int getSize() {
        return size;
    }

    public boolean isSelected() {
        return selected;
    }
}
