package common;

public class FailResult<T> extends Result<T> {

    public FailResult(String error) {

	super(false, null, error);
    }
}
