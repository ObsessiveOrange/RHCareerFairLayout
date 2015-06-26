package adt;

public abstract class Result<T> {
    
    public final boolean success;
    public final T       result;
    public final String  error;
    
    protected Result(boolean success, T result, String error) {
    
        this.success = success;
        this.result = result;
        this.error = error;
    }
    
    @Override
    public String toString() {
    
        return success ? result.toString() : error.toString();
    }
}
