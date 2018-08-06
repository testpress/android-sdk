package in.testpress.network;

import java.util.HashMap;

import in.testpress.core.TestpressSession;
import in.testpress.models.ProfileDetails;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import static in.testpress.network.TestpressApiClient.PROFILE_DETAILS_PATH;

public interface AuthenticationService {

    @POST("{authenticate_url}")
    RetrofitCall<TestpressSession> authenticate(
            @Path(value = "authenticate_url", encoded = true) String authenticateUrlFrag,
            @Body HashMap<String, String> arguments);

    @GET(PROFILE_DETAILS_PATH)
    RetrofitCall<ProfileDetails> getProfileDetails();

}
