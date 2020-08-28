package in.testpress.exam.util;

import androidx.fragment.app.FragmentActivity;
import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressUserDetails;
import in.testpress.models.ProfileDetails;

public class Watermark {
    private ProfileDetails profileDetails = TestpressUserDetails.getInstance().getProfileDetails();

    public String get(FragmentActivity activity) {
        if (hasProfileDetails()) {
            return getUserDetail(profileDetails);
        } else {
            return getUserDetailsFromNetwork(activity);
        }
    }

    private boolean hasProfileDetails() {
        return profileDetails != null;
    }

    private String getUserDetail(ProfileDetails profileDetails) {
        if (hasEmail(profileDetails)) {
            return profileDetails.getEmail();
        } else {
            return profileDetails.getUsername();
        }
    }

    private boolean hasEmail(ProfileDetails profileDetails) {
        return profileDetails.getEmail() != null && !profileDetails.getEmail().isEmpty();
    }

    private String watermark = " ";
    private String getUserDetailsFromNetwork(FragmentActivity activity) {
        TestpressUserDetails.getInstance().load(activity, new TestpressCallback<ProfileDetails>() {
            @Override
            public void onSuccess(ProfileDetails profileDetails) {
                watermark = getUserDetail(profileDetails);
            }
            @Override
            public void onException(TestpressException exception) {
            }
        });
        return watermark;
    }
}