package in.testpress.core;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.webkit.CookieManager;
import android.webkit.WebStorage;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import in.testpress.models.InstituteSettings;
import in.testpress.models.ProfileDetails;
import in.testpress.network.AuthorizationErrorResponse;
import in.testpress.network.TestpressApiClient;
import in.testpress.R;
import in.testpress.util.Assert;
import in.testpress.util.UIUtils;
import io.sentry.Sentry;
import io.sentry.android.core.SentryAndroid;
import io.sentry.protocol.User;

public final class TestpressSdk {

    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;

    public static final int COURSE_CHAPTER_REQUEST_CODE = 1002;
    public static final int COURSE_CONTENT_LIST_REQUEST_CODE = 1003;
    public static final int COURSE_CONTENT_DETAIL_REQUEST_CODE = 1004;

    public static final int PERMISSIONS_REQUEST_CODE = 600;

    public static final String ACTION_PRESSED_HOME = "pressedHomeButton";

    private static final String KEY_TESTPRESS_AUTH_TOKEN = "testpressAuthToken";
    private static final String KEY_TESTPRESS_SHARED_PREFS = "testpressSharedPreferences";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_TESTPRESS_USER_ID = "testpressUserId";
    private static final String KEY_COURSE_DATABASE_SESSION = "courseDatabaseSession";
    private static final String KEY_EXAM_DATABASE_SESSION = "examDatabaseSession";
    public static final String TESTPRESS_SDK_DATABASE = "testpressSdkDB";
    private static final String RUBIK_REGULAR_FONT_PATH = "Rubik-Regular.ttf";
    private static final String RUBIK_MEDIUM_FONT_PATH = "Rubik-Medium.ttf";
    public enum Provider { FACEBOOK, GOOGLE, TESTPRESS }
    private static Typeface sRubikRegular;
    private static Typeface sRubikMedium;

    private static SharedPreferences getPreferences(Context context) {
        if (pref == null) {
            validateContext(context);
            pref = context.getSharedPreferences(KEY_TESTPRESS_SHARED_PREFS, Context.MODE_PRIVATE);
        }
        return pref;
    }

    @SuppressLint("CommitPrefEdits")
    private static SharedPreferences.Editor getPreferenceEditor(Context context) {
        if (editor == null) {
            validateContext(context);
            editor = getPreferences(context).edit();
        }
        return editor;
    }

    public static void setTestpressSession(@NonNull Context context,
                                            @NonNull TestpressSession testpressSession) {
        if (testpressSession == null) {
            throw new IllegalArgumentException("TestpressSession must not be null.");
        }
        SharedPreferences.Editor editor = getPreferenceEditor(context);
        editor.putString(KEY_TESTPRESS_AUTH_TOKEN, TestpressSession.serialize(testpressSession));
        editor.apply();
    }

    @Nullable
    public static TestpressSession getTestpressSession(@NonNull Context context) {
        SharedPreferences pref = getPreferences(context);
        return TestpressSession.deserialize(pref.getString(KEY_TESTPRESS_AUTH_TOKEN, ""));
    }

    public static void clearActiveSession(@NonNull Context context) {
        if (TestpressSdk.getTestpressSession(context) != null) {
            new TestpressApiClient(context, TestpressSdk.getTestpressSession(context)).logout().enqueue(new TestpressCallback<Void>() {
                @Override
                public void onSuccess(Void result) {}

                @Override
                public void onException(TestpressException exception) {}
            });
        }

        SharedPreferences.Editor editor = getPreferenceEditor(context);
        editor.clear().commit();
        // Clear webView session
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        WebStorage.getInstance().deleteAllData();
    }

    public static boolean hasActiveSession(@NonNull Context context) {
        return getTestpressSession(context) != null;
    }

    public static void setTestpressCourseDBSession(@NonNull Context context,
                                                   @NonNull String sessionToken) {
        //noinspection ConstantConditions
        if (sessionToken == null || sessionToken.isEmpty()) {
            throw new IllegalArgumentException("SessionToken must not be null or Empty.");
        }
        SharedPreferences.Editor editor = getPreferenceEditor(context);
        editor.putString(KEY_COURSE_DATABASE_SESSION, sessionToken);
        editor.apply();
    }

    @Nullable
    private static String getTestpressCourseDBSession(@NonNull Context context) {
        SharedPreferences pref = getPreferences(context);
        return pref.getString(KEY_COURSE_DATABASE_SESSION, null);
    }

    public static boolean isNewCourseDBSession(@NonNull Context context,
                                               @NonNull String sessionToken) {
        //noinspection ConstantConditions
        if (sessionToken == null || sessionToken.isEmpty()) {
            throw new IllegalArgumentException("SessionToken must not be null or Empty.");
        }
        String existingDBSessionToken = getTestpressCourseDBSession(context);
        return existingDBSessionToken == null ||
                !sessionToken.equals(existingDBSessionToken);
    }

    public static void setTestpressExamDBSession(@NonNull Context context,
                                                 @NonNull String sessionToken) {
        //noinspection ConstantConditions
        if (sessionToken == null || sessionToken.isEmpty()) {
            throw new IllegalArgumentException("SessionToken must not be null or Empty.");
        }
        SharedPreferences.Editor editor = getPreferenceEditor(context);
        editor.putString(KEY_EXAM_DATABASE_SESSION, sessionToken);
        editor.apply();
    }

    public static void setTestpressUserId(@NonNull Context context, int id) {
        getPreferenceEditor(context).putInt(KEY_TESTPRESS_USER_ID, id).apply();
    }

    public static boolean isTestpressUserIdExist(@NonNull Context context) {
        return getPreferences(context).getInt(KEY_TESTPRESS_USER_ID, 0) != 0;
    }

    public static int getTestpressUserId(@NonNull Context context) {
        return getPreferences(context).getInt(KEY_TESTPRESS_USER_ID, 0);
    }

    @Nullable
    private static String getTestpressExamDBSession(@NonNull Context context) {
        SharedPreferences pref = getPreferences(context);
        return pref.getString(KEY_EXAM_DATABASE_SESSION, null);
    }

    public static boolean isNewExamDBSession(@NonNull Context context,
                                             @NonNull String sessionToken) {
        //noinspection ConstantConditions
        if (sessionToken == null || sessionToken.isEmpty()) {
            throw new IllegalArgumentException("SessionToken must not be null or Empty.");
        }
        String existingDatabaseSessionToken = getTestpressExamDBSession(context);
        return existingDatabaseSessionToken == null ||
                !sessionToken.equals(existingDatabaseSessionToken);
    }

    /**
     * Load the font from the given path.
     *
     * @param context Context
     * @param fontPath path of the Typeface.
     * @return Typeface created from the given path.
     * @throws IllegalArgumentException if typeface couldn't be load from the given path.
     */
    public static Typeface getTypeface(@NonNull Context context, @NonNull String fontPath) {
        validateContext(context);
        //noinspection ConstantConditions
        if (fontPath == null || fontPath.isEmpty()) {
            throw new IllegalArgumentException("FontPath must not be null.");
        }
        try {
            return Typeface.createFromAsset(context.getAssets(), fontPath);
        } catch (Exception e) {
            throw new IllegalStateException("Could not get typeface '" + fontPath + "' because "
                    + e.getMessage());
        }
    }

    /**
     * @param context Context
     * @return RubikRegular Typeface
     */
    public static Typeface getRubikRegularFont(@NonNull Context context) {
        if (sRubikRegular == null) {
            sRubikRegular = getTypeface(context, RUBIK_REGULAR_FONT_PATH);
        }
        return sRubikRegular;
    }

    /**
     * @param context Context
     * @return RubikMedium Typeface
     */
    public static Typeface getRubikMediumFont(@NonNull Context context) {
        if (sRubikMedium == null) {
            sRubikMedium = getTypeface(context, RUBIK_MEDIUM_FONT_PATH);
        }
        return sRubikMedium;
    }

    /**
     * Authorize the user and store the testpress session
     *
     * @param context Context
     * @param instituteSettings Institute settings contains base url & flags to enable custom features
     * @param userId User's social account id (if provider is fb or google)
     *               or testpress username(for Provider.TESTPRESS)
     * @param accessToken User's social account access token(if provider is fb or google)
     *                    or testpress password(for Provider.TESTPRESS)
     * @param provider Provider.FACEBOOK or Provider.GOOGLE or Provider.TESTPRESS
     */
    public static void initialize(@NonNull Context context, @NonNull InstituteSettings instituteSettings,
                                  @NonNull String userId, @NonNull String accessToken,
                                  @NonNull Provider provider) {
        initialize(context, instituteSettings, userId, accessToken, provider, null);
    }

    /**
     * Authorize the user and store the testpress session
     *
     * @param context Context
     * @param instituteSettings Institute settings contains base url & flags to enable custom features
     * @param userId User's social account id (if provider is fb or google)
     *               or testpress username(for Provider.TESTPRESS)
     * @param accessToken User's social account access token(if provider is fb or google)
     *                    or testpress password(for Provider.TESTPRESS)
     * @param provider Provider.FACEBOOK or Provider.GOOGLE or Provider.TESTPRESS
     * @param callback Callback which will be call on success or failure
     */
    public static void initialize(@NonNull final Context context,
                                  @NonNull final InstituteSettings instituteSettings,
                                  @NonNull final String userId,
                                  @NonNull final String accessToken,
                                  @NonNull final Provider provider,
                                  final TestpressCallback<TestpressSession> callback) {
        validateContext(context);
        if (userId == null || accessToken == null || provider == null) {
            throw new IllegalArgumentException("UserId & AccessToken & Provider must not be null.");
        }
        Assert.assertNotNull("InstituteSettings must not be null.", instituteSettings);
        if (userId.equals(getPreferences(context).getString(KEY_USER_ID, null)) &&
                hasActiveSession(context)) {
            if (callback != null) {
                callback.onSuccess(getTestpressSession(context));
            }
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.testpress_please_wait));
        progressDialog.setCancelable(false);
        UIUtils.setIndeterminateDrawable(context, progressDialog, 4);
        progressDialog.show();

        String urlPath;
        HashMap<String, String> credentials = new HashMap<String, String>();
        if (provider == Provider.TESTPRESS) {
            urlPath = TestpressApiClient.TESTPRESS_AUTH_PATH;
            credentials.put("username", userId);
            credentials.put("password", accessToken);
        } else {
            urlPath = TestpressApiClient.SOCIAL_AUTH_PATH;
            credentials.put("provider", provider.name());
            credentials.put("user_id", userId);
            credentials.put("access_token", accessToken);
        }
        new TestpressApiClient(instituteSettings.getBaseUrl(), context)
                .authenticate(urlPath, credentials)
                .enqueue(new TestpressCallback<TestpressSession>() {
                    @Override
                    public void onSuccess(TestpressSession testpressSession) {
                        testpressSession.setInstituteSettings(instituteSettings);
                        setTestpressSession(context, testpressSession);
                        SharedPreferences.Editor editor = getPreferenceEditor(context);
                        editor.putString(KEY_USER_ID, userId);
                        editor.apply();
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        if (callback != null) {
                            callback.onSuccess(testpressSession);
                        }
                        initSentry(context,instituteSettings.getAndroidSentryDns());
                    }

                    @Override
                    public void onException(TestpressException testpressException) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        if (callback != null) {
                            if (testpressException.isClientError()) {
                                AuthorizationErrorResponse errorResponse = testpressException.getErrorBodyAs(
                                        testpressException.getResponse(), AuthorizationErrorResponse.class);
                                String message = "";
                                if (!errorResponse.getUserId().isEmpty()) {
                                    message = errorResponse.getUserId().get(0);
                                } else if (!errorResponse.getAccessToken().isEmpty()) {
                                    message = errorResponse.getAccessToken().get(0);
                                } else if (!errorResponse.getProvider().isEmpty()) {
                                    message = errorResponse.getProvider().get(0);
                                } else if (!errorResponse.getNonFieldErrors().isEmpty()) {
                                    message = errorResponse.getNonFieldErrors().get(0);
                                } else if (!errorResponse.getUsername().isEmpty()) {
                                    message = errorResponse.getUsername().get(0);
                                }
                                testpressException.setMessage(message);
                            }
                            callback.onException(testpressException);
                        }
                    }
                });
    }

    private static void validateContext(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
    }

    public static void initSentry(Context context,String androidSentryDns) {
        SentryAndroid.init(
                context,
                options -> {
                    options.setDsn(androidSentryDns);
                    options.setEnableAutoSessionTracking(true);
                });
        SetSentryUserDetails(context);
    }

    private static void SetSentryUserDetails(Context context) {
        TestpressUserDetails.getInstance().load(context, new TestpressCallback<ProfileDetails>() {
            @Override
            public void onSuccess(ProfileDetails userDetails) {
                User user = new User();
                user.setId(userDetails.getId().toString());
                user.setEmail(userDetails.getEmail());
                user.setUsername(userDetails.getUsername());
                Map<String,String> otherDetails = new HashMap<>();
                otherDetails.put("package_name",context.getPackageName());
                user.setOthers(otherDetails);
                Sentry.setUser(user);
            }

            @Override
            public void onException(TestpressException exception) {
            }
        });
    }
}
