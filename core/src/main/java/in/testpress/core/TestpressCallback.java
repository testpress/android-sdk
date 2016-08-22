package in.testpress.core;

public abstract class TestpressCallback<T> {

    public abstract void onSuccess(T result);

    public abstract void onException(TestpressException exception);

}
