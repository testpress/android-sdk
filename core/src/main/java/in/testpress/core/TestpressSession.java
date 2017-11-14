package in.testpress.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import in.testpress.models.InstituteSettings;
import in.testpress.util.Assert;

public class TestpressSession {
    private InstituteSettings instituteSettings;
    private String token;

    public TestpressSession(@NonNull InstituteSettings instituteSettings, @NonNull String token) {
        setInstituteSettings(instituteSettings);
        setToken(token);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("AuthToken must not be null or Empty.");
        }
        this.token = token;
    }

    public InstituteSettings getInstituteSettings() {
        return instituteSettings;
    }

    public void setInstituteSettings(@NonNull InstituteSettings instituteSettings) {
        Assert.assertNotNull("InstituteSettings must not be null.", instituteSettings);
        this.instituteSettings = instituteSettings;
    }

    public static String serialize(@NonNull TestpressSession session) {
        if (session == null) {
            throw new IllegalArgumentException("TestpressSession must not be null.");
        }
        Gson gson = new Gson();
        return gson.toJson(session);
    }

    @Nullable
    public static TestpressSession deserialize(String serializedSession) {
        if (serializedSession != null && !serializedSession.isEmpty()) {
            try {
                Gson gson = new Gson();
                return gson.fromJson(serializedSession, TestpressSession.class);
            } catch (Exception e) {
                Log.d("TestpressSession", e.getMessage(), e);
            }
        }
        return null;
    }
}
