package in.testpress.network;

import java.io.IOException;

import in.testpress.core.TestpressCallback;
import retrofit2.Response;

public interface RetrofitCall<T> {

    void cancel();

    void enqueue(TestpressCallback<T> callback);

    RetrofitCall<T> clone();

    Response<T> execute() throws IOException;

}
