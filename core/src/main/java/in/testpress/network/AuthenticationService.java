package in.testpress.network;

import java.util.HashMap;

import in.testpress.core.TestpressAuthToken;
import retrofit.http.Body;
import retrofit.http.POST;

public interface AuthenticationService {

    @POST("/api/v2/auth-token/")
    TestpressAuthToken authenticate(@Body HashMap<String, String> arguments);

}
