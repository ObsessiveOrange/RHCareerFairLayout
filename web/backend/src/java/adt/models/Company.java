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

public class Company extends Entry implements Comparable<Company> {

    protected String name;
    protected String description;
    protected String websiteLink;
    protected String address;

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

    public Company(ResultSet rs) throws SQLException {

	this(rs.getLong("id"), rs.getString("name"), rs.getString("description"), rs.getString("websiteLink"),
		rs.getString("address"));
    }

    private Company(Long id, String name, String description, String websiteLink, String address) {

	super(id);
	this.name = name;
	this.description = description;
	this.websiteLink = websiteLink;
	this.address = address;
    }

    public Company(String name, String description, String websiteLink, String address) {

	this.name = name;
	this.description = description;
	this.websiteLink = websiteLink;
	this.address = address;
    }

    public String getName() {

	return name;
    }

    public void setName(String name) {

	this.name = name;
    }

    public String getDescription() {

	return description;
    }

    public void setDescription(String description) {

	this.description = description;
    }

    public String getWebsiteLink() {

	return websiteLink;
    }

    public void setWebsiteLink(String websiteLink) {

	this.websiteLink = websiteLink;
    }

    public String getAddress() {

	return address;
    }

    public void setAddress(String address) {

	this.address = address;
    }

    @Override
    public int compareTo(Company other) {

	return name.compareTo(other.getName());
    }

    public Result insertIntoDB(long termId) throws SQLException, ClassNotFoundException {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    stmt = conn.prepareCall("CALL Data_Insert_Company(?, ?, ?, ?, ?)");
	    stmt.setString(1, name);
	    stmt.setString(2, description == null ? "" : description);
	    stmt.setString(3, websiteLink == null ? "" : websiteLink);
	    stmt.setString(4, address == null ? "" : address);
	    stmt.setLong(5, termId);

	    rs = stmt.executeQuery();

	    if (!(respObj = Utils.checkResultSuccess(rs, 500)).isSuccess()) {
		return respObj;
	    }

	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {
		    this.id = rs.getLong(rs.findColumn("newCompanyId"));
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
