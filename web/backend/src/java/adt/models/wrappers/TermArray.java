package adt.models.wrappers;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import adt.models.Term;
import common.Result;
import common.Result.SuccessResult;
import managers.SQLManager;
import misc.Utils;
import servlets.ServletLog;

public class TermArray extends ArrayList<Term> {

    /**
     * 
     */
    private static final long serialVersionUID = -4617007484637352031L;

    public static Result getTermList() throws ClassNotFoundException, SQLException {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {

	    Result respObj;

	    conn = SQLManager.getConn();

	    stmt = conn.prepareCall("CALL Data_Get_TermList()");

	    rs = stmt.executeQuery();

	    if (!(respObj = Utils.checkResultSuccess(rs)).isSuccess()) {
		return respObj;
	    }

	    TermArray termList = new TermArray();

	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {
		    Term t = new Term(rs.getInt("year"), rs.getString("quarter"));
		    termList.add(t);
		}
	    }

	    respObj = new SuccessResult();
	    respObj.put("termList", termList);

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
