package in.testpress.util;

public abstract class Callback<T> {

    public abstract void onSuccess(T result);

    public abstract void onException(Exception exception);

}
