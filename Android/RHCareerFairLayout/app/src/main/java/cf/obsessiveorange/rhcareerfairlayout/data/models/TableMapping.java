package cf.obsessiveorange.rhcareerfairlayout.data.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.fasterxml.jackson.annotation.JsonProperty;

import cf.obsessiveorange.rhcareerfairlayout.data.DBAdapter;

public class TableMapping extends Entry implements Comparable<TableMapping> {

    private Long companyId;
    private Integer size;


    public TableMapping(Cursor c) {

        super(c.getLong(c.getColumnIndexOrThrow(DBAdapter.KEY_ID)));

        this.companyId = c.getLong(c.getColumnIndexOrThrow(DBAdapter.KEY_COMPANY_ID));
        this.size = c.getInt(c.getColumnIndexOrThrow(DBAdapter.KEY_SIZE));
    }

    public TableMapping(@JsonProperty("id") Long id,
                        @JsonProperty("companyId") Long companyId,
                        @JsonProperty("size") Integer size) {

        super(id);
        this.setCompanyId(companyId);
        this.setSize(size);
    }

    /**
     * @return the companyId
     */
    public Long getCompanyId() {
        return companyId;
    }

    /**
     * @param companyId the companyId to set
     */
    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    /**
     * @return the tableSize
     */
    public Integer getSize() {
        return size;
    }

    /**
     * @param tableSize the tableSize to set
     */
    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public int compareTo(TableMapping other) {

        return id.compareTo(other.getId());
    }

    public ContentValues toContentValues() {
        ContentValues row = new ContentValues();

        row.put(DBAdapter.KEY_ID, this.getId());
        row.put(DBAdapter.KEY_COMPANY_ID, this.getCompanyId());
        row.put(DBAdapter.KEY_SIZE, this.getSize());

        return row;
    }
}
