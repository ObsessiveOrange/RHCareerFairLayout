package common;

public class SuccessResult<T> extends Result<T> {
    
    public SuccessResult(T result) {
    
        super(true, result, null);
    }
    
}
