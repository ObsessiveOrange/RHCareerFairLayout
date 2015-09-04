package adt.models;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import common.Result;
import common.Result.SuccessResult;
import managers.SQLManager;
import misc.Utils;
import servlets.ServletLog;

public class CompanyCategory {

    protected Long companyId;
    protected HashSet<Long> categories;

    /**
     * Constructor for reconstructing from SQL queries
     * 
     * @param id
     *            The ID of the company
     * @param name
     *            The name of the company
     * @param description
     *            A description of the company (Can be null)
     * @param tableNumber
     *            The table the company will be at.
     */

    public CompanyCategory(ResultSet rs) throws SQLException {

	this(rs.getLong("companyId"), rs.getLong("categoryId"));
    }

    public CompanyCategory(Long companyId, Long categoryId) {

	this.companyId = companyId;
	this.categories = new HashSet<Long>();
	this.categories.add(categoryId);
    }

    public Long getCompanyId() {

	return companyId;
    }

    public void setCompanyId(Long companyId) {

	this.companyId = companyId;
    }

    public HashSet<Long> getCategories() {

	return categories;
    }

    public void addCategoryId(Long categoryId) {

	this.categories.add(categoryId);
    }

    public void addCategories(HashSet<Long> categories) {

	this.categories.addAll(categories);
    }

    public Result insertIntoDB() throws SQLException, ClassNotFoundException {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    stmt = conn.prepareCall("CALL Data_Insert_CompanyCategory(?, ?)");

	    for (Long categoryId : categories) {
		stmt.setLong(1, companyId);
		stmt.setLong(2, categoryId);

		rs = stmt.executeQuery();

		if (!(respObj = Utils.checkResultSuccess(rs, 500)).isSuccess()) {
		    return respObj;
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
