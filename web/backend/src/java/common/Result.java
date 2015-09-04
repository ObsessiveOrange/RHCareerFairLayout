package common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class Result extends HashMap<String, Object> {
    /**
     * 
     */
    private static final long serialVersionUID = 1043878399393774201L;

    @JsonIgnore
    protected ArrayList<Cookie> cookies = null;
    @JsonIgnore
    private final int status;

    public Result(int status) {

	this.status = status;
	this.put("timestamp", System.currentTimeMillis());
    }

    public boolean isSuccess() {
	return status == 200;
    }

    public int getStatus() {
	return status;
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

    public Response toJAXRS() {

	return Response.status(this.getStatus()).entity(this).build();
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

    public static class SuccessResult extends Result {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6108222601410083578L;

	public SuccessResult() {

	    super(200);
	}

	public SuccessResult(String message) {

	    super(200);
	    put("message", message);
	}

	public Result addCookie(String key, String value) {

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

    public static class FailResult extends Result {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1029372537435325034L;

	public FailResult(int status) {

	    super(status);
	}

	public FailResult(Integer status, String message) {

	    super(status);
	    put("message", message);
	}

	public FailResult(Exception exception) {

	    super(500);
	    put("exception message", exception.getMessage());
	    put("exception stack trace", exception.getStackTrace());
	}
    }
}
