package in.testpress.core;

import in.testpress.models.TestpressApiResponse;
import in.testpress.network.RetrofitCall;

public abstract class TestpressRetrofitRequest<T> {
    public abstract RetrofitCall<TestpressApiResponse<T>> getRetrofitCall(int page, int size);
}
