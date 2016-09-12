package in.testpress.network;

import java.util.HashMap;

import in.testpress.core.TestpressSession;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;

public interface AuthenticationService {

    @POST("/{authenticate_url}")
    TestpressSession authenticate(
            @Path(value = "authenticate_url", encode = false) String authenticateUrlFrag,
            @Body HashMap<String, String> arguments);

}
