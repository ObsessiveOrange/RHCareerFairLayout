package adt.deprecated;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import common.FailResult;
import common.Result;
import common.SuccessResult;

public class RequestBody {
    
    private final LinkedTreeMap<String, Object> map;
    
    public RequestBody(LinkedTreeMap<String, Object> map) {
    
        this.map = map;
    }
    
    public RequestBody(HttpServletRequest request) throws IOException {
    
        StringBuffer sb = new StringBuffer();
        
        String line = null;
        BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null)
            sb.append(line);
        
        this.map = getJsonMapFromString(sb.toString());
    }
    
    public RequestBody(String jsonString) throws IOException {
    
        this.map = getJsonMapFromString(jsonString);
    }
    
    private LinkedTreeMap<String, Object> getJsonMapFromString(String jsonString) {
    
        Gson gson = new Gson();
        Type mapType = new TypeToken<LinkedTreeMap<String, Object>>() {
        }.getType();
        return gson.fromJson(jsonString, mapType);
    }
    
    @SuppressWarnings("unchecked")
    public <T> Result<T> getItem(String key, T returnType) {
    
        return validateItem(key) ? new SuccessResult<T>((T) map.get(key)) : new FailResult<T>("Key not found");
        
    }
    
    public Result<Integer> getInt(String key, Integer min, Integer max) {
    
        if (validateItem(key)) {
            Object obj = map.get(key);
            Integer i;
            if (obj instanceof Integer) {
                i = (Integer) obj;
            }
            else {
                i = Integer.valueOf(obj.toString());
            }
            if ((min != null && i < min) || (max != null && i > max)) {
                
                return new FailResult<Integer>(key + " does not satisfy constraints");
            }
            else {
                return new SuccessResult<Integer>(i);
            }
        }
        return new FailResult<Integer>("Key not found");
    }
    
    public Result<Double> getDouble(String key, Double min, Double max) {
    
        if (validateItem(key)) {
            Object obj = map.get(key);
            Double i;
            if (obj instanceof Double) {
                i = (Double) obj;
            }
            else {
                i = Double.valueOf(obj.toString());
            }
            if ((min != null && i < min) || (max != null && i > max)) {
                
                return new FailResult<Double>(key + " does not satisfy constraints");
            }
            else {
                return new SuccessResult<Double>(i);
            }
        }
        return new FailResult<Double>("Key not found");
    }
    
    public Result<String> getString(String key, Integer minLength, Integer maxLength) {
    
        if (validateItem(key)) {
            Object obj = map.get(key);
            String s;
            if (obj instanceof String) {
                s = (String) obj;
            }
            else {
                s = obj.toString();
            }
            
            if ((minLength != null && s.length() < minLength) || (maxLength != null && s.length() > maxLength)) {
                
                return new FailResult<String>(key + " does not satisfy constraints");
            }
            else {
                return new SuccessResult<String>(s);
            }
        }
        return new FailResult<String>("Key not found");
    }
    
    @SuppressWarnings("unchecked")
    public Result<RequestBody> getMap(String key) {
    
        if (validateItem(key) && map.get(key) instanceof LinkedTreeMap) {
            return new SuccessResult<RequestBody>(new RequestBody((LinkedTreeMap<String, Object>) map.get(key)));
        }
        return new FailResult<RequestBody>("Key not found");
    }
    
    @SuppressWarnings("unchecked")
    public <T> Result<ArrayList<T>> getArray(String key, T type) {
    
        if (validateItem(key)) {
            return new SuccessResult<ArrayList<T>>((ArrayList<T>) map.get(key));
        }
        return new FailResult<ArrayList<T>>("Key not found");
    }
    
    public boolean validateItems(String... keys) {
    
        for (String key : keys) {
            if (!validateItem(key)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean validateItem(String key) {
    
        return map.containsKey(key);
    }
}
