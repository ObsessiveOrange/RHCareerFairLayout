package adt.models;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TableMapping extends Entry implements Comparable<TableMapping> {

    private Long companyId;
    private Integer size;

    public TableMapping(ResultSet rs) throws SQLException {

	this(rs.getLong("id"), rs.getLong("companyId"), rs.getInt("size"));
    }

    public TableMapping(Long id, Long companyId, Integer size) {

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
     * @param companyId
     *            the companyId to set
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
     * @param tableSize
     *            the tableSize to set
     */
    public void setSize(Integer size) {
	this.size = size;
    }

    @Override
    public int compareTo(TableMapping other) {

	return id.compareTo(other.getId());
    }
}
