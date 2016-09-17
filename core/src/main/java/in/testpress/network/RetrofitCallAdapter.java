package in.testpress.network;

/**
 * References
 * https://gist.github.com/rahulgautam/25c72ffcac70dacb87bd#file-errorhandlingexecutorcalladapterfactory-java
 * https://github.com/square/retrofit/tree/master/samples/src/main/java/com/example/retrofit
 */

import java.io.IOException;
import java.util.concurrent.Executor;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitCallAdapter<T> implements RetrofitCall<T> {
    private final Call<T> call;
    private final Executor callbackExecutor;

    RetrofitCallAdapter(Call<T> call, Executor callbackExecutor) {
        this.call = call;
        this.callbackExecutor = callbackExecutor;
    }

    @Override public void cancel() {
        call.cancel();
    }

    @Override public void enqueue(final TestpressCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override public void onResponse(final Call<T> call, final Response<T> response) {
                if (response.isSuccessful()) {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(response.body());
                        }
                    });
                } else {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onException(TestpressException.httpError(response));
                        }
                    });
                }
            }

            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            @Override public void onFailure(final Call<T> call, final Throwable throwable) {
                TestpressException exception;
                if (throwable instanceof IOException) {
                    exception = TestpressException.networkError((IOException) throwable);
                } else {
                    exception = TestpressException.unexpectedError(throwable);
                    exception.printStackTrace();
                }
                final TestpressException finalException = exception;
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onException(finalException);
                    }
                });
            }
        });
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override public RetrofitCall<T> clone() {
        return new RetrofitCallAdapter<T>(call.clone(), callbackExecutor);
    }

    @Override
    public Response<T> execute() throws IOException {
        return call.execute();
    }
}
