package servlets;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import common.Response;
import common.Response.FailResponse;
import common.Response.SuccessResponse;
import managers.SQLManager;
import misc.BCrypt;
import misc.Utils;

@Path("/users")
public class Users {
    private static final int SESSION_VALID_DAYS = 7;

    @Context
    private HttpServletResponse response;

    @POST
    @Produces("application/json")
    @Path("login")
    public Response login(@HeaderParam("authUser") String authUser, @HeaderParam("authPass") String authPass,
	    @HeaderParam("User-Agent") String userAgent) {

	try {
	    CallableStatement stmt;
	    ResultSet rs;
	    Response respObj;

	    // Check session is valid
	    stmt = SQLManager.getConn().prepareCall("CALL User_GetHashedPw(?);");
	    stmt.setString(1, authUser);

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs)).isSuccess()) {
		return respObj;
	    }

	    // loop over all remaining result sets, checking if contains
	    // password hashes - should only have 1.
	    checkPwLoop: while ((rs = Utils.getNextResultSet(stmt)) != null) {
		if (Utils.hasColumn(rs, "hashedPw")) {
		    // loop over all hashed passwords, checking there are any
		    // positive matches - should only have 1 item here.
		    boolean hasNext = rs.next();
		    while (hasNext) {
			if (BCrypt.checkpw(authPass, rs.getString("hashedPw"))) {
			    break checkPwLoop;
			} else if (!hasNext) {
			    return new FailResponse("Invalid Username/Password Combination");
			}
			hasNext = rs.next();
		    }
		}
	    }

	    // if we got to here, we are authenticated.
	    String authToken = BCrypt.hashpw(authUser + System.currentTimeMillis(), BCrypt.gensalt());

	    Response newSessionResponse;
	    if (!(newSessionResponse = startNewSession(authUser, authToken, userAgent, response)).isSuccess()) {
		return newSessionResponse;
	    }

	    return new SuccessResponse();
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    @POST
    @Produces("application/json")
    @Path("check_authentication")
    public Response checkAuthentication(@CookieParam("authUser") String authUser,
	    @CookieParam("authToken") String authToken, @HeaderParam("User-Agent") String userAgent) {
	return checkAuthenticationHelper(authUser, authToken, userAgent, response);
    }

    private Response checkAuthenticationHelper(String authUser, String authToken, String userAgent,
	    HttpServletResponse response) {

	try {
	    CallableStatement stmt;
	    ResultSet rs;
	    Response respObj;

	    // Check session is valid
	    stmt = SQLManager.getConn().prepareCall("CALL User_CheckAuth(?, ?, ?);");
	    stmt.setString(1, authUser);
	    stmt.setString(2, authToken);
	    stmt.setString(3, userAgent == null ? "NO USER-AGENT PROVIDED" : userAgent);

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs)).isSuccess()) {
		return respObj;
	    }

	    Response newSessionResponse;
	    if (!(newSessionResponse = startNewSession(authUser, authToken, userAgent, response)).isSuccess()) {
		return newSessionResponse;
	    }

	    return new SuccessResponse();

	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    private Response startNewSession(String authUser, String authToken, String userAgent,
	    HttpServletResponse response) {

	try {
	    CallableStatement stmt;
	    ResultSet rs;
	    Response respObj;

	    // Update session's expiry date.
	    stmt = SQLManager.getConn().prepareCall("CALL User_NewSession(?, ?, ?, ?);");
	    stmt.setString(1, authUser);
	    stmt.setString(2, authToken);
	    stmt.setString(3, userAgent == null ? "NO USER-AGENT PROVIDED" : userAgent);
	    stmt.setInt(4, SESSION_VALID_DAYS);

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs)).isSuccess()) {
		return respObj;
	    }

	    if (response != null) {
		Cookie authUserCookie = new Cookie("authUser", authUser);
		authUserCookie.setSecure(true);
		authUserCookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(SESSION_VALID_DAYS));

		Cookie authTokenCookie = new Cookie("authToken", authToken);
		authTokenCookie.setSecure(true);
		authTokenCookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(SESSION_VALID_DAYS));

		response.addCookie(authUserCookie);
		response.addCookie(authTokenCookie);
	    }

	    return new SuccessResponse();
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    @POST
    @Produces("application/json")
    @Path("logout")
    public Response logout(@CookieParam("authUser") String authUser, @CookieParam("authToken") String authToken,
	    @HeaderParam("User-Agent") String userAgent) {

	try {
	    CallableStatement stmt;
	    ResultSet rs;
	    Response respObj;

	    Response authResponse;
	    if (!(authResponse = checkAuthenticationHelper(authUser, authToken, userAgent, null)).isSuccess()) {
		return authResponse;
	    }

	    stmt = SQLManager.getConn().prepareCall("CALL User_Logout(?, ?, ?);");
	    stmt.setString(1, authUser);
	    stmt.setString(2, authToken);
	    stmt.setString(3, userAgent == null ? "NO USER-AGENT PROVIDED" : userAgent);

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs)).isSuccess()) {
		return respObj;
	    }

	    Cookie authUserCookie = new Cookie("authUser", "");
	    authUserCookie.setSecure(true);
	    authUserCookie.setMaxAge(0);

	    Cookie authTokenCookie = new Cookie("authToken", "");
	    authTokenCookie.setSecure(true);
	    authTokenCookie.setMaxAge(0);

	    return new SuccessResponse();
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }

    @POST
    @Produces("application/json")
    @Path("register")
    public Response register(@HeaderParam("authUser") String authUser, @HeaderParam("authPass") String authPass,
	    @HeaderParam("User-Agent") String userAgent) {

	try {
	    CallableStatement stmt;
	    ResultSet rs;
	    Response respObj;

	    String hashedPw = BCrypt.hashpw(authPass, BCrypt.gensalt());

	    stmt = SQLManager.getConn().prepareCall("CALL User_Register(?, ?);");
	    stmt.setString(1, authUser);
	    stmt.setString(2, hashedPw);

	    rs = stmt.executeQuery();
	    if (!(respObj = Utils.checkResultSuccess(rs)).isSuccess()) {
		return respObj;
	    }

	    return new SuccessResponse(authUser + " successfully registered.");
	} catch (Exception e) {
	    ServletLog.logEvent(e);

	    return new FailResponse(e);
	}
    }
}
