package adt.models;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.annotation.JsonProperty;

import common.Result;
import common.Result.SuccessResult;
import managers.SQLManager;
import misc.Utils;
import servlets.ServletLog;

public class TableMapping extends Entry implements Comparable<TableMapping> {

    private Long companyId;
    private Integer size;

    public TableMapping(ResultSet rs) throws SQLException {

	this(rs.getLong("id"), null, rs.getInt("size"));
	Long companyId = rs.getLong("companyId");
	if (!rs.wasNull()) {
	    setCompanyId(companyId);
	}
    }

    public TableMapping(@JsonProperty("id") Long id, @JsonProperty("companyId") Long companyId,
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

    public Result insertIntoDB(long termId) throws SQLException, ClassNotFoundException {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    System.out.println("Adding tableMapping: " + id + ", " + companyId + ", " + size + ", " + termId);

	    stmt = conn.prepareCall("CALL Data_Insert_TableMapping(?, ?, ?, ?)");
	    stmt.setLong(1, id);

	    if (companyId != null) {
		stmt.setLong(2, companyId);
	    } else {
		stmt.setNull(2, java.sql.Types.INTEGER);
	    }

	    stmt.setInt(3, size);
	    stmt.setLong(4, termId);

	    rs = stmt.executeQuery();

	    if (!(respObj = Utils.checkResultSuccess(rs, 500)).isSuccess()) {
		return respObj;
	    }

	    return new SuccessResult();
	} finally {
	    if (rs != null) {
		try {
		    rs.close();
		} catch (SQLException e) {
		    ServletLog.logEvent(e);
		}
	    }
	    if (stmt != null) {
		try {
		    stmt.close();
		} catch (SQLException e) {
		    ServletLog.logEvent(e);
		}
	    }
	    if (conn != null) {
		try {
		    conn.close();
		} catch (SQLException e) {
		    ServletLog.logEvent(e);
		}
	    }
	}
    }
}
