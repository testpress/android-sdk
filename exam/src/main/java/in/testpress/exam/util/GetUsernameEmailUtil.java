package in.testpress.exam.util;

import androidx.fragment.app.FragmentActivity;
import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressUserDetails;
import in.testpress.models.ProfileDetails;

public class GetUsernameEmailUtil {
    static String watermarkText;
    public static String getEmailOrUsername(FragmentActivity activity) {
        ProfileDetails profileDetails = TestpressUserDetails.getInstance().getProfileDetails();
        if (profileDetails != null) {
            watermarkText = getEmailOrUsername(profileDetails);
        } else {
            TestpressUserDetails.getInstance().load(activity, new TestpressCallback<ProfileDetails>() {
                @Override
                public void onSuccess(ProfileDetails userDetails) {
                    watermarkText = getEmailOrUsername(userDetails);
                }

                @Override
                public void onException(TestpressException exception) {
                }
            });
        }
        if (watermarkText != null) {
            return watermarkText;
        } else {
            return "";
        }
    }

    public static String getEmailOrUsername(ProfileDetails profileDetails) {
        String watermark;
        if (profileDetails.getEmail() != null && !profileDetails.getEmail().isEmpty()) {
            watermark = profileDetails.getEmail();
        } else {
            watermark = profileDetails.getUsername();
        }
        return watermark;
    }
}