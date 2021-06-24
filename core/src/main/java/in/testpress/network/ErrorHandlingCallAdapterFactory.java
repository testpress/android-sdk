package in.testpress.network;

import androidx.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

public class ErrorHandlingCallAdapterFactory extends CallAdapter.Factory {
    @Override public CallAdapter<RetrofitCall<?>, ?> get(Type returnType, Annotation[] annotations,
                                                Retrofit retrofit) {
        if (getRawType(returnType) != RetrofitCall.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalStateException(
                    "RetrofitCall must have generic type (e.g., RetrofitCall<ResponseBody>)");
        }
        final Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
        final Executor callbackExecutor = retrofit.callbackExecutor();
        return new ErrorHandlingCallAdapter<>(responseType, callbackExecutor);
    }

    private static final class ErrorHandlingCallAdapter<R> implements CallAdapter<R, RetrofitCallAdapter<R>> {
        private final Type responseType;
        private final Executor callbackExecutor;

        ErrorHandlingCallAdapter(Type responseType, Executor callbackExecutor) {
            this.responseType = responseType;
            this.callbackExecutor = callbackExecutor;
        }

        @Override public Type responseType() {
            return responseType;
        }

        @Override public RetrofitCallAdapter<R> adapt(Call<R> call) {
            return new RetrofitCallAdapter<>(call, callbackExecutor);
        }
    }
}
