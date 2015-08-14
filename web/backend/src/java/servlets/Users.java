package servlets;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import common.Result;
import common.Result.FailResult;
import common.Result.SuccessResult;
import managers.SQLManager;
import misc.BCrypt;
import misc.Utils;

@Path("/users")
public class Users {
    private static final int SESSION_VALID_DAYS = 7;

    @Context
    private HttpServletResponse response;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("login")
    public Response login(@HeaderParam("authUser") String authUser, @HeaderParam("authPass") String authPass,
	    @HeaderParam("User-Agent") String userAgent) {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    // Check session is valid
	    stmt = conn.prepareCall("CALL User_GetHashedPw(?);");
	    stmt.setString(1, authUser);

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs, 401)).isSuccess()) {
		return respObj.toJAXRS();
	    }

	    // loop over all remaining result sets, checking if contains
	    // password hashes - should only have 1.
	    checkPwLoop: while ((rs = Utils.getNextResultSet(stmt)) != null) {
		if (Utils.hasColumn(rs, "hashedPw")) {
		    // loop over all hashed passwords, checking there are any
		    // positive matches - should only have 1 item here.
		    while (rs.next()) {
			if (BCrypt.checkpw(authPass, rs.getString("hashedPw"))) {
			    break checkPwLoop;
			}
		    }
		    return new FailResult(401, "Invalid Username/Password Combination").toJAXRS();
		}
	    }

	    // if we got to here, we are authenticated.
	    String authToken = BCrypt.hashpw(authUser + System.currentTimeMillis(), BCrypt.gensalt());

	    Result newSessionResponse;
	    if (!(newSessionResponse = startNewSession(authUser, authToken, userAgent, response)).isSuccess()) {
		return newSessionResponse.toJAXRS();
	    }

	    return new SuccessResult().toJAXRS();
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("check_authentication")
    public Response checkAuthentication(@CookieParam("authUser") String authUser,
	    @CookieParam("authToken") String authToken, @HeaderParam("User-Agent") String userAgent) {
	return checkAuthenticationHelper(authUser, authToken, userAgent, 0, response).toJAXRS();
    }

    public static Result checkAuthenticationHelper(String authUser, String authToken, String userAgent,
	    int permissionsLevel, HttpServletResponse response) {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    // Check session is valid
	    stmt = conn.prepareCall("CALL User_CheckAuth(?, ?, ?, ?);");
	    stmt.setString(1, authUser);
	    stmt.setString(2, authToken);
	    stmt.setString(3, userAgent == null ? "NO USER-AGENT PROVIDED" : userAgent);
	    stmt.setInt(4, permissionsLevel);

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs, 401)).isSuccess()) {
		return respObj;
	    }

	    Result newSessionResponse;
	    if (!(newSessionResponse = startNewSession(authUser, authToken, userAgent, response)).isSuccess()) {
		return newSessionResponse;
	    }

	    return new SuccessResult();

	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResult(e);
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

    private static Result startNewSession(String authUser, String authToken, String userAgent,
	    HttpServletResponse response) {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    // Update session's expiry date.
	    stmt = conn.prepareCall("CALL User_NewSession(?, ?, ?, ?);");
	    stmt.setString(1, authUser);
	    stmt.setString(2, authToken);
	    stmt.setString(3, userAgent == null ? "NO USER-AGENT PROVIDED" : userAgent);
	    stmt.setInt(4, SESSION_VALID_DAYS);

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs, 401)).isSuccess()) {
		return respObj;
	    }

	    if (response != null) {
		Cookie authUserCookie = new Cookie("authUser", authUser);
		authUserCookie.setSecure(true);
		authUserCookie.setPath("/api/users");
		authUserCookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(SESSION_VALID_DAYS));

		Cookie authTokenCookie = new Cookie("authToken", authToken);
		authTokenCookie.setSecure(true);
		authTokenCookie.setPath("/api/users");
		authTokenCookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(SESSION_VALID_DAYS));

		response.addCookie(authUserCookie);
		response.addCookie(authTokenCookie);
	    }

	    return new SuccessResult();
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResult(e);
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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("logout")
    public Response logout(@CookieParam("authUser") String authUser, @CookieParam("authToken") String authToken,
	    @HeaderParam("User-Agent") String userAgent) {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    Result authResponse;
	    if (!(authResponse = checkAuthenticationHelper(authUser, authToken, userAgent, 0, null)).isSuccess()) {
		return authResponse.toJAXRS();
	    }

	    stmt = conn.prepareCall("CALL User_Logout(?, ?, ?);");
	    stmt.setString(1, authUser);
	    stmt.setString(2, authToken);
	    stmt.setString(3, userAgent == null ? "NO USER-AGENT PROVIDED" : userAgent);

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs, 500)).isSuccess()) {
		return respObj.toJAXRS();
	    }

	    Cookie authUserCookie = new Cookie("authUser", "");
	    authUserCookie.setSecure(true);
	    authUserCookie.setMaxAge(0);

	    Cookie authTokenCookie = new Cookie("authToken", "");
	    authTokenCookie.setSecure(true);
	    authTokenCookie.setMaxAge(0);

	    response.addCookie(authUserCookie);
	    response.addCookie(authTokenCookie);

	    return new SuccessResult().toJAXRS();
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();
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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("register")
    public Response register(@HeaderParam("authUser") String authUser, @HeaderParam("authPass") String authPass,
	    @HeaderParam("User-Agent") String userAgent) {
	Connection conn = null;
	CallableStatement stmt = null;
	ResultSet rs = null;

	try {
	    Result respObj;

	    conn = SQLManager.getConn();

	    String hashedPw = BCrypt.hashpw(authPass, BCrypt.gensalt());

	    stmt = conn.prepareCall("CALL User_Register(?, ?);");
	    stmt.setString(1, authUser);
	    stmt.setString(2, hashedPw);

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs, 400)).isSuccess()) {
		return respObj.toJAXRS();
	    }

	    return new SuccessResult(authUser + " successfully registered.").toJAXRS();
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResult(e).toJAXRS();
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
