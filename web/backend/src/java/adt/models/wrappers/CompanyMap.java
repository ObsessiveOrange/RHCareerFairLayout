package adt.models.wrappers;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import adt.models.Company;
import common.Result;
import common.Result.SuccessResult;
import managers.SQLManager;
import misc.Utils;
import servlets.ServletLog;

public class CompanyMap extends HashMap<Long, Company> {

    /**
     * 
     */
    private static final long serialVersionUID = -4617007484637352031L;

    public static Result getCompanies(Long termId) throws ClassNotFoundException, SQLException {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    stmt = conn.prepareCall("CALL Data_Get_CompanyList(?)");
	    stmt.setLong(1, termId);

	    rs = stmt.executeQuery();

	    if (!(respObj = Utils.checkResultSuccess(rs, 500)).isSuccess()) {
		return respObj;
	    }

	    CompanyMap companyMap = new CompanyMap();

	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {
		    Company c = new Company(rs);

		    companyMap.put(c.getId(), c);
		}
	    }

	    respObj = new SuccessResult();
	    respObj.put("companyMap", companyMap);

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
