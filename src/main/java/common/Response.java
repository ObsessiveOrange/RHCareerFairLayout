package common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class Response extends HashMap<String, Object> {
    /**
     * 
     */
    private static final long serialVersionUID = 1043878399393774201L;
    @JsonIgnore
    public ArrayList<Cookie> cookies = null;

    public Response(int status, long timestamp) {

	this.put("status", status);
	this.put("timestamp", timestamp);
    }

    public boolean isSuccess() {
	return getInt("status") == 1;
    }

    public Integer getInt(String key) {
	Object returnItem = get(key);
	if (returnItem instanceof Integer) {
	    return (Integer) returnItem;
	} else {
	    return Integer.valueOf(String.valueOf(returnItem));
	}
    }

    public Long getLong(String key) {
	Object returnItem = get(key);
	if (returnItem instanceof Long) {
	    return (Long) returnItem;
	} else {
	    return Long.valueOf(String.valueOf(returnItem));
	}
    }

    public BigDecimal getBigDecimal(String key) {
	Object returnItem = get(key);
	if (returnItem instanceof BigDecimal) {
	    return (BigDecimal) returnItem;
	} else {
	    return new BigDecimal(String.valueOf(returnItem));
	}
    }

    public Double getDouble(String key) {
	Object returnItem = get(key);
	if (returnItem instanceof Double) {
	    return (Double) returnItem;
	} else {
	    return Double.valueOf(String.valueOf(returnItem));
	}
    }

    public Boolean getBoolean(String key) {
	Object returnItem = get(key);
	if (returnItem instanceof Boolean) {
	    return (Boolean) returnItem;
	} else {
	    return Boolean.valueOf(String.valueOf(returnItem));
	}
    }

    public String getString(String key) {
	Object returnItem = get(key);
	if (returnItem instanceof Integer) {
	    return (String) returnItem;
	} else {
	    return String.valueOf(returnItem);
	}
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> returnClass) {

	Object returnItem = get(key);
	if (returnItem != null) {

	    if (returnItem.getClass().equals(returnClass)) {
		return (T) returnItem;
	    } else if (returnClass.equals(Integer.class)) {
		return (T) Integer.valueOf(String.valueOf(returnItem));
	    } else if (returnClass.equals(Long.class)) {
		return (T) Long.valueOf(String.valueOf(returnItem));
	    } else if (returnClass.equals(Double.class)) {
		return (T) Double.valueOf(String.valueOf(returnItem));
	    } else if (returnClass.equals(BigDecimal.class)) {
		return (T) new BigDecimal(String.valueOf(returnItem));
	    } else if (returnClass.equals(Boolean.class)) {
		return (T) Boolean.valueOf(String.valueOf(returnItem));
	    } else if (returnClass.equals(String.class)) {
		return (T) String.valueOf(returnItem);
	    } else {
		return returnClass.cast(returnItem);
	    }
	}
	return null;
    }

    public static class SuccessResponse extends Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6108222601410083578L;

	public SuccessResponse() {

	    super(1, System.currentTimeMillis());
	}

	public SuccessResponse(String message) {

	    super(1, System.currentTimeMillis());
	    put("message", message);
	}

	public Response addCookie(String key, String value) {

	    if (cookies == null) {
		cookies = new ArrayList<Cookie>();
	    }

	    Cookie newCookie = new Cookie(key, value);
	    newCookie.setSecure(true);
	    newCookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(30));

	    cookies.add(newCookie);

	    return this;
	}
    }

    public static class FailResponse extends Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1029372537435325034L;

	public FailResponse(int errorCode) {

	    super(errorCode, System.currentTimeMillis());
	}

	public FailResponse(String message) {

	    super(-1, System.currentTimeMillis());
	    put("errorCode", -1);
	    put("message", message);
	}

	public FailResponse(int errorCode, String message) {

	    super(errorCode, System.currentTimeMillis());
	    put("message", message);
	}

	public FailResponse(Exception exception) {

	    super(-1, System.currentTimeMillis());
	    put("exception message", exception.getMessage());
	    put("exception stack trace", exception.getStackTrace());
	}
    }
}
