package servlets;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.Response;

public class ServletUtils {

    public static void sendResponse(HttpServletResponse connection, Response response) throws IOException {

	if (response.isSuccess() && response.cookies != null) {
	    for (Cookie c : response.cookies) {
		connection.addCookie(c);
	    }
	}
	connection.getWriter().print(response);
    }

    public static String getBodyData(HttpServletRequest request) throws IOException {

	StringBuilder sb = new StringBuilder();

	BufferedReader r = request.getReader();
	String s = null;
	while ((s = r.readLine()) != null) {
	    sb.append(s);
	}

	return sb.toString();
    }
}
