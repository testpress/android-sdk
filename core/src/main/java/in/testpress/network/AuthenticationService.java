package in.testpress.network;

import java.util.HashMap;

import in.testpress.core.TestpressSession;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AuthenticationService {

    @POST("{authenticate_url}")
    RetrofitCall<TestpressSession> authenticate(
            @Path(value = "authenticate_url", encoded = true) String authenticateUrlFrag,
            @Body HashMap<String, String> arguments);

}
