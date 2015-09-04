package cf.obsessiveorange.rhcareerfairlayout.data.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.fasterxml.jackson.annotation.JsonProperty;

import cf.obsessiveorange.rhcareerfairlayout.data.managers.DBManager;

public class Category extends Entry implements Comparable<Category> {
    protected String name;
    protected String type;

    public Category(Cursor c) {

        super(c.getLong(c.getColumnIndexOrThrow(DBManager.KEY_ID)));

        this.name = c.getString(c.getColumnIndexOrThrow(DBManager.KEY_NAME));
        this.type = c.getString(c.getColumnIndexOrThrow(DBManager.KEY_TYPE));
    }

    public Category(@JsonProperty("id") Long id, @JsonProperty("name") String name, @JsonProperty("type") String type) {

        super(id);

        this.name = name;
        this.type = type;

    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    @Override
    public int compareTo(Category other) {

        return name.compareTo(other.getName());
    }

    public ContentValues toContentValues() {
        ContentValues row = new ContentValues();

        row.put(DBManager.KEY_ID, this.getId());
        row.put(DBManager.KEY_NAME, this.getName());
        row.put(DBManager.KEY_TYPE, this.getType());

        return row;
    }
}
