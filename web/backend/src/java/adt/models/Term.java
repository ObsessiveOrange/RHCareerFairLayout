package adt.models;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import common.Result;
import common.Result.SuccessResult;
import managers.SQLManager;
import misc.Utils;
import servlets.ServletLog;

@JsonInclude(Include.NON_NULL)
public class Term extends Entry implements Comparable<Term> {

    private Integer year;
    private String quarter;
    private Integer layout_Section1;
    private Integer layout_Section2;
    private Integer layout_Section2_PathWidth;
    private Integer layout_Section2_Rows;
    private Integer layout_Section3;
    private boolean active = false;

    public static Result getTerm(long termId) throws ClassNotFoundException, SQLException {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    stmt = conn.prepareCall("CALL Data_Get_Term(?);");
	    stmt.setLong(1, termId);

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs, 500)).isSuccess()) {
		return respObj;
	    }

	    Term term = null;

	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {

		    term = new Term(rs);

		}
	    }

	    respObj = new SuccessResult();
	    respObj.put("term", term);

	    return respObj;
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

    public Term(Integer year, String quarter, Boolean active) {
	this(null, year, quarter, null, null, null, null, null, active);
    }

    public Term(ResultSet rs) throws SQLException {

	this(rs.getLong("id"), rs.getInt("year"), rs.getString("quarter"), rs.getInt("layout_Section1"),
		rs.getInt("layout_Section2"), rs.getInt("layout_Section2_PathWidth"), rs.getInt("layout_Section2_Rows"),
		rs.getInt("layout_Section3"), rs.getBoolean("active"));
    }

    public Term(Long id, Integer year, String quarter, Integer layout_Section1, Integer layout_Section2,
	    Integer layout_Section2_PathWidth, Integer layout_Section2_Rows, Integer layout_Section3, Boolean active) {

	super(id);
	this.year = year;
	this.quarter = quarter;
	this.setLayout_Section1(layout_Section1);
	this.setLayout_Section2(layout_Section2);
	this.setLayout_Section2_PathWidth(layout_Section2_PathWidth);
	this.setLayout_Section2_Rows(layout_Section2_Rows);
	this.setLayout_Section3(layout_Section3);
	this.active = active;
    }

    @Override
    public int compareTo(Term o) {

	int result = this.year.compareTo(o.year);
	if (result == 0) {
	    switch (this.quarter) {
	    case "Spring":
		return -1;
	    case "Fall":
		switch (o.quarter) {
		case "Spring":
		    return 1;
		case "Winter":
		    return -1;
		default:
		    return 0;
		}
	    case "Winter":
		switch (o.quarter) {
		case "Spring":
		case "Fall":
		    return 1;
		default:
		    return 0;
		}
	    }

	}
	return result;
    }

    /**
     * @return the layout_Section1
     */
    public Integer getYear() {
	return year;
    }

    /**
     * @param layout_Section1
     *            the layout_Section1 to set
     */
    public void setYear(Integer year) {
	this.year = year;
    }

    /**
     * @return the layout_Section1
     */
    public String getQuarter() {
	return quarter;
    }

    /**
     * @param layout_Section1
     *            the layout_Section1 to set
     */
    public void setQuarter(String quarter) {
	this.quarter = quarter;
    }

    /**
     * @return the layout_Section1
     */
    public Integer getLayout_Section1() {
	return layout_Section1;
    }

    /**
     * @param layout_Section1
     *            the layout_Section1 to set
     */
    public void setLayout_Section1(Integer layout_Section1) {
	this.layout_Section1 = layout_Section1;
    }

    /**
     * @return the layout_Section2
     */
    public Integer getLayout_Section2() {
	return layout_Section2;
    }

    /**
     * @param layout_Section2
     *            the layout_Section2 to set
     */
    public void setLayout_Section2(Integer layout_Section2) {
	this.layout_Section2 = layout_Section2;
    }

    /**
     * @return the layout_Section2_PathWidth
     */
    public Integer getLayout_Section2_PathWidth() {
	return layout_Section2_PathWidth;
    }

    /**
     * @param layout_Section2_PathWidth
     *            the layout_Section2_PathWidth to set
     */
    public void setLayout_Section2_PathWidth(Integer layout_Section2_PathWidth) {
	this.layout_Section2_PathWidth = layout_Section2_PathWidth;
    }

    /**
     * @return the layout_Section2_Rows
     */
    public Integer getLayout_Section2_Rows() {
	return layout_Section2_Rows;
    }

    /**
     * @param layout_Section2_Rows
     *            the layout_Section2_Rows to set
     */
    public void setLayout_Section2_Rows(Integer layout_Section2_Rows) {
	this.layout_Section2_Rows = layout_Section2_Rows;
    }

    /**
     * @return the layout_Section3
     */
    public Integer getLayout_Section3() {
	return layout_Section3;
    }

    /**
     * @param layout_Section3
     *            the layout_Section3 to set
     */
    public void setLayout_Section3(Integer layout_Section3) {
	this.layout_Section3 = layout_Section3;
    }

    public Result insertIntoDB() throws SQLException, ClassNotFoundException {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    stmt = conn.prepareCall("CALL Data_Insert_Term(?, ?, ?, ?, ?, ?, ?, ?)");
	    stmt.setInt(1, year);
	    stmt.setString(2, quarter);
	    stmt.setInt(3, layout_Section1);
	    stmt.setInt(4, layout_Section2);
	    stmt.setInt(5, layout_Section2_PathWidth);
	    stmt.setInt(6, layout_Section2_Rows);
	    stmt.setInt(7, layout_Section3);
	    stmt.setBoolean(8, active);

	    rs = stmt.executeQuery();

	    if (!(respObj = Utils.checkResultSuccess(rs, 500)).isSuccess()) {
		return respObj;
	    }

	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {
		    this.id = rs.getLong(rs.findColumn("newTermId"));
		}
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

    public Boolean getActive() {
	return active;
    }

    public Result setActive(boolean active) throws SQLException, ClassNotFoundException {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    stmt = conn.prepareCall("CALL Data_Update_Term_SetActive(?, ?)");
	    stmt.setLong(1, getId());
	    stmt.setBoolean(2, active);

	    rs = stmt.executeQuery();

	    if (!(respObj = Utils.checkResultSuccess(rs, 500)).isSuccess()) {
		return respObj;
	    }

	    this.active = true;

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
