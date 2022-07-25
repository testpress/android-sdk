package in.testpress.network;

import java.util.HashMap;

import in.testpress.core.TestpressSession;
import in.testpress.models.ProfileDetails;
import in.testpress.models.SSOUrl;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import static in.testpress.network.TestpressApiClient.PROFILE_DETAILS_PATH;
import static in.testpress.network.TestpressApiClient.LOGOUT_PATH;
import static in.testpress.network.TestpressApiClient.URL_GENERATE_SSO_LINK;

public interface AuthenticationService {

    @POST("{authenticate_url}")
    RetrofitCall<TestpressSession> authenticate(
            @Path(value = "authenticate_url", encoded = true) String authenticateUrlFrag,
            @Body HashMap<String, String> arguments);

    @GET(PROFILE_DETAILS_PATH)
    RetrofitCall<ProfileDetails> getProfileDetails();

    @POST(LOGOUT_PATH)
    RetrofitCall<Void> logout();

    @POST(URL_GENERATE_SSO_LINK)
    RetrofitCall<SSOUrl> getSsoUrl();
}
