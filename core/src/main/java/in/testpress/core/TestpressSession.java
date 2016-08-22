package in.testpress.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

public class TestpressSession {
    protected String baseUrl;
    protected String token;

    public TestpressSession(@NonNull String baseUrl, @NonNull String token) {
        setBaseUrl(baseUrl);
        setToken(token);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("BaseUrl must not be null or Empty.");
        }
        this.baseUrl = baseUrl;
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
