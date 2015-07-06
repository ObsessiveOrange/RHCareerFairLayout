package servlets;

import java.sql.CallableStatement;

import managers.SQLManager;

public class ServletLog {

    public static void logEvent(ServletEvent event) {

	try {
	    CallableStatement stmt;

	    stmt = SQLManager.getConn().prepareCall("CALL Log_LogEvent(?, ?, ?);");
	    stmt.setString(1, event.type);
	    stmt.setString(2, event.message);
	    stmt.setString(3, event.detailMessage);

	    stmt.executeQuery();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    System.err.println("Error logging event");
	    e.printStackTrace();
	}

    }

    public static void logEvent(String type, String message, String detailMessage) {

	ServletEvent event = new ServletEvent(type, message, detailMessage);
	logEvent(event);
    }

    public static void logEvent(Exception e) {

	ServletEvent event = new ServletEvent("Exception", e.getMessage(), e.getStackTrace().toString());
	logEvent(event);
    }

    public static class ServletEvent {

	private final String type, message, detailMessage;

	public ServletEvent(String type, String message, String detailMessage) {
	    this.type = type;
	    this.message = message;
	    this.detailMessage = detailMessage;
	}

	/**
	 * @return the type
	 */
	public String getType() {
	    return type;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
	    return message;
	}

	/**
	 * @return the detailMessage
	 */
	public String getDetailMessage() {
	    return detailMessage;
	}
    }
}
