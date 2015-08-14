package servlets;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/users/admin")
public class Admin {

    @Context
    private HttpServletResponse response;

    @GET
    @Produces("application/json")
    @Path("check_authentication")
    public Response checkAuthentication(@CookieParam("authUser") String authUser,
	    @CookieParam("authToken") String authToken, @HeaderParam("User-Agent") String userAgent) {
	return Users.checkAuthenticationHelper(authUser, authToken, userAgent, 10, response).toJAXRS();
    }
}
