package in.testpress.network;

import java.util.HashMap;

import in.testpress.core.TestpressSession;
import retrofit.http.Body;
import retrofit.http.POST;

public interface AuthenticationService {

    @POST("/api/v2.2/social-auth/")
    TestpressSession authenticate(@Body HashMap<String, String> arguments);

}
