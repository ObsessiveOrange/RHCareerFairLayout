package adt;

public class Table extends Entry implements Comparable<Table> {

    private Long companyId;
    private Integer tableSize;

    public Table(Long id, Long companyId, Integer tableSize) {

	super(id);
	this.setCompanyId(companyId);
	this.setTableSize(tableSize);
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
    public Integer getTableSize() {
	return tableSize;
    }

    /**
     * @param tableSize
     *            the tableSize to set
     */
    public void setTableSize(Integer tableSize) {
	this.tableSize = tableSize;
    }

    @Override
    public int compareTo(Table other) {

	return id.compareTo(other.getId());
    }
}
