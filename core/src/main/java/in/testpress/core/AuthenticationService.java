package in.testpress.core;

import java.util.HashMap;

import in.testpress.models.AuthToken;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthenticationService {

    @POST("/api/v2/auth-token/")
    Call<AuthToken> authenticate(@Body HashMap<String, String> arguments);

}
