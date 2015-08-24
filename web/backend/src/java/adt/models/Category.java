package adt.models;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.Result;
import common.Result.SuccessResult;
import managers.SQLManager;
import misc.Utils;
import servlets.ServletLog;

public class Category extends Entry implements Comparable<Category> {
    protected String type;
    protected String name;

    public Category(ResultSet rs) throws SQLException {

	this(rs.getLong("id"), rs.getString("name"), rs.getString("type"));
    }

    private Category(Long id, String name, String type) {

	super(id);

	this.name = name;
	this.type = type;

    }

    public Category(String name, String type) {

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

    public Result insertIntoDB() throws SQLException, ClassNotFoundException {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    stmt = conn.prepareCall("CALL Data_Insert_Category(?, ?)");
	    stmt.setString(1, name);
	    stmt.setString(2, type);

	    rs = stmt.executeQuery();

	    if (!(respObj = Utils.checkResultSuccess(rs, 500)).isSuccess()) {
		return respObj;
	    }

	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {
		    this.id = rs.getLong(rs.findColumn("newCategoryId"));
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
}
