package adt.models.wrappers;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import adt.models.Category;
import common.Result;
import common.Result.SuccessResult;
import managers.SQLManager;
import misc.Utils;
import servlets.ServletLog;

public class CategoryMap extends HashMap<Long, Category> {

    /**
     * 
     */
    private static final long serialVersionUID = -4617007484637352031L;

    public static Result getCategories() throws ClassNotFoundException, SQLException {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    stmt = conn.prepareCall("CALL Data_Get_CategoryList()");

	    rs = stmt.executeQuery();

	    if (!(respObj = Utils.checkResultSuccess(rs, 500)).isSuccess()) {
		return respObj;
	    }

	    CategoryMap categoryMap = new CategoryMap();

	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {
		    Category c = new Category(rs);

		    categoryMap.put(c.getId(), c);
		}
	    }

	    respObj = new SuccessResult();
	    respObj.put("categoryMap", categoryMap);

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
}
