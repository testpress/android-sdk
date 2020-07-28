package in.testpress.exam.util;

import androidx.fragment.app.FragmentActivity;
import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressUserDetails;
import in.testpress.models.ProfileDetails;

public class EmailOrUsernameUtil {
    static String emailOrUsernameText = "";
    public static String getEmailOrUsernameWatermark(FragmentActivity activity) {
        ProfileDetails profileDetails = TestpressUserDetails.getInstance().getProfileDetails();
        if (profileDetails != null) {
            emailOrUsernameText = getEmailOrUsername(profileDetails);
        } else {
            TestpressUserDetails.getInstance().load(activity, new TestpressCallback<ProfileDetails>() {
                @Override
                public void onSuccess(ProfileDetails userDetails) {
                    emailOrUsernameText = getEmailOrUsername(userDetails);
                }

                @Override
                public void onException(TestpressException exception) {
                }
            });
        }
        return emailOrUsernameText;
    }

    public static String getEmailOrUsername(ProfileDetails profileDetails) {
        if (profileDetails.getEmail() != null && !profileDetails.getEmail().isEmpty()) {
            return profileDetails.getEmail();
        } else {
            return profileDetails.getUsername();
        }
    }
}