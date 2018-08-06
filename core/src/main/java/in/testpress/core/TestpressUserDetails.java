package in.testpress.core;

import android.content.Context;

import in.testpress.models.ProfileDetails;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;

public class TestpressUserDetails {

    private static TestpressUserDetails userDetails;

    private ProfileDetails profileDetails;
    private TestpressCallback<ProfileDetails> callBack;
    private RetrofitCall<ProfileDetails> retrofitCall;

    public static TestpressUserDetails getInstance() {
        if (userDetails == null) {
            userDetails = new TestpressUserDetails();
        }
        return userDetails;
    }

    public ProfileDetails getProfileDetails() {
        return profileDetails;
    }

    public void setProfileDetails(ProfileDetails profileDetails) {
        this.profileDetails = profileDetails;
    }

    public void load(Context context, TestpressCallback<ProfileDetails> testpressCallback) {
        callBack = testpressCallback;
        load(context);
    }

    public void load(Context context) {
        if (!TestpressSdk.hasActiveSession(context)) {
            return;
        }
        if (retrofitCall != null) {
            retrofitCall.cancel();
        }
        retrofitCall = new TestpressApiClient(context, TestpressSdk.getTestpressSession(context))
                .getProfileDetails()
                .enqueue(new TestpressCallback<ProfileDetails>() {
                    @Override
                    public void onSuccess(ProfileDetails profileDetails) {
                        TestpressUserDetails.this.profileDetails = profileDetails;
                        if (callBack != null) {
                            callBack.onSuccess(profileDetails);
                        }
                    }

                    @Override
                    public void onException(TestpressException testpressException) {
                        if (callBack != null) {
                            callBack.onException(testpressException);
                        }
                    }
                });
    }

}
