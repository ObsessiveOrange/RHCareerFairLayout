package misc;

import javax.servlet.http.HttpServletRequest;

import adt.Response;
import adt.Response.FailResponse;
import adt.Response.SuccessResponse;

public class Utils {
    
    public static String sanitizeString(String input) {
    
        return input.replaceAll("\\W", "");
    }
    
    public static Response validateStrings(Integer minLength, Integer maxLength, HttpServletRequest request, String... headers) {
    
        SuccessResponse resp = new SuccessResponse();
        
        for (String h : headers) {
            String s = request.getHeader(h);
            
            if (s == null) {
                return new FailResponse("Invalid value provided for required header " + h + ".");
            }
            else if (minLength != null && s.length() < minLength) {
                return new FailResponse("Invalid value provided for required header " + h + ".");
            }
            else if (maxLength != null && s.length() > maxLength) {
                return new FailResponse("Invalid value provided for required header " + h + ".");
            }
            else {
                resp.addToReturnData(h, s);
            }
        }
        return resp;
    }
    
    public static Response validateIntegers(Integer minValue, Integer maxValue, HttpServletRequest request, String... headers) {
    
        SuccessResponse resp = new SuccessResponse();
        for (String h : headers) {
            Integer value;
            
            try {
                value = Integer.valueOf(request.getHeader(h));
            } catch (NumberFormatException e) {
                return new FailResponse("Invalid value provided for required header " + h + ".");
            }
            
            if (value == null) {
                return new FailResponse("Invalid value provided for required header " + h + ".");
            }
            else if (minValue != null && value < minValue) {
                return new FailResponse("Invalid value provided for required header " + h + ".");
            }
            else if (maxValue != null && value > maxValue) {
                return new FailResponse("Invalid value provided for required header " + h + ".");
            }
            else {
                resp.addToReturnData(h, value);
            }
        }
        return resp;
    }
    
    public static Response validateDoubles(Double minValue, Double maxValue, HttpServletRequest request, String... headers) {
    
        SuccessResponse resp = new SuccessResponse();
        for (String h : headers) {
            Double value;
            
            try {
                value = Double.valueOf(request.getHeader(h));
            } catch (NumberFormatException e) {
                return new FailResponse("Invalid value provided for required header " + h + ".");
            }
            
            if (value == null || value == Double.NaN || value == Double.NEGATIVE_INFINITY || value == Double.POSITIVE_INFINITY) {
                return new FailResponse("Invalid value provided for required header " + h + ".");
            }
            else if (minValue != null && value < minValue) {
                return new FailResponse("Invalid value provided for required header " + h + ".");
            }
            else if (maxValue != null && value > maxValue) {
                return new FailResponse("Invalid value provided for required header " + h + ".");
            }
            else {
                resp.addToReturnData(h, value);
            }
        }
        return resp;
    }
    
    public static Response validateObjects(Object... objects) {
    
        for (Object o : objects) {
            if (o == null) {
                return new FailResponse("Invalid value provided for required object: null");
            }
        }
        return new SuccessResponse();
    }
    
    public static Response validateTerm(String year, String quarter) {
    
        if (!(year.matches("\\d{4}") && quarter.matches("(?i:Spring|Fall|Winter)"))) {
            return new FailResponse("Invalid year/quarter format.");
        }
        return new SuccessResponse();
    }
    
    public static String toCamelCase(String s) {
    
        String[] parts = s.split("_");
        StringBuilder camelCaseString = new StringBuilder();
        for (String part : parts) {
            camelCaseString.append(toProperCase(part));
        }
        return camelCaseString.toString();
    }
    
    public static String toCamelCaseWithUnderscore(String s) {
    
        String[] parts = s.split("_");
        StringBuilder camelCaseString = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            camelCaseString.append(toProperCase(parts[i]));
            if (i < parts.length - 1) {
                camelCaseString.append("_");
            }
        }
        return camelCaseString.toString();
    }
    
    public static String toProperCase(String s) {
    
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
    
    public static String getDBName(String year, String quarter) {
    
        return year + "_" + toProperCase(quarter);
    }
    
}
