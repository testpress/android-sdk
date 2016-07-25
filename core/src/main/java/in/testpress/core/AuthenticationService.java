package in.testpress.core;

import java.util.HashMap;

import in.testpress.models.AuthToken;
import retrofit.http.Body;
import retrofit.http.POST;

public interface AuthenticationService {

    @POST("/api/v2/auth-token/")
    AuthToken authenticate(@Body HashMap<String, String> arguments);

}
