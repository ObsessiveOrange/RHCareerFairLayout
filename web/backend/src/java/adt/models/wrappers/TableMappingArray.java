package adt.models.wrappers;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import adt.models.TableMapping;
import common.Result;
import common.Result.SuccessResult;
import managers.SQLManager;
import misc.Utils;
import servlets.ServletLog;

public class TableMappingArray extends ArrayList<TableMapping> {

    /**
     * 
     */
    private static final long serialVersionUID = -4617007484637352031L;

    public static Result getTableMappings(Long termId) throws ClassNotFoundException, SQLException {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    stmt = conn.prepareCall("CALL Data_Get_TableMappingList(?);");
	    stmt.setLong(1, termId);

	    rs = stmt.executeQuery();

	    if (!(respObj = Utils.checkResultSuccess(rs, 500)).isSuccess()) {
		return respObj;
	    }

	    TableMappingArray tableMappingList = new TableMappingArray();

	    if ((rs = Utils.getNextResultSet(stmt)) != null) {
		while (rs.next()) {
		    TableMapping table = new TableMapping(rs);

		    tableMappingList.add(table);
		}
	    }

	    respObj = new SuccessResult();
	    respObj.put("tableMappingList", tableMappingList);

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
