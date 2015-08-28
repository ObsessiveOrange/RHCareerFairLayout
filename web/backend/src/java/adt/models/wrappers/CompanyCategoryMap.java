package adt.models.wrappers;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import adt.models.CompanyCategory;
import common.Result;
import common.Result.SuccessResult;
import managers.SQLManager;
import misc.Utils;
import servlets.ServletLog;

public class CompanyCategoryMap extends HashMap<Long, CompanyCategory> {

    /**
     * 
     */
    private static final long serialVersionUID = -4617007484637352031L;

    public static Result getCompanyCategories(Long termId) throws ClassNotFoundException, SQLException {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    stmt = conn.prepareCall("CALL Data_Get_CompanyCategoryList(?)");
	    stmt.setLong(1, termId);

	    rs = stmt.executeQuery();

	    if (!(respObj = Utils.checkResultSuccess(rs, 500)).isSuccess()) {
		return respObj;
	    }

	    CompanyCategoryMap companyCategoryMap = new CompanyCategoryMap();

	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {
		    CompanyCategory c = new CompanyCategory(rs);

		    if (companyCategoryMap.get(c.getCompanyId()) != null) {
			companyCategoryMap.get(c.getCompanyId()).addCategories(c.getCategories());
		    } else {
			companyCategoryMap.put(c.getCompanyId(), c);
		    }
		}
	    }

	    respObj = new SuccessResult();
	    respObj.put("companyCategoryMap", companyCategoryMap);

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
