package in.testpress.util;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This interceptor put all the Cookies in Preferences in the Request.
 */
public class AddCookiesInterceptor implements Interceptor {
    public static final String PREF_COOKIES = "PREF_COOKIES";
    public static final String CSRF_TOKEN = "csrftoken";
    public static final String SESSION_ID = "sessionid";
    public static final String X_CSRFToken = "X-CSRFToken";

    private Context context;

    public AddCookiesInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();

        HashSet<String> preferences = (HashSet<String>) PreferenceManager.getDefaultSharedPreferences(context).getStringSet(PREF_COOKIES, new HashSet<String>());

        String cookieString = "";
        for (String cookie : preferences) {
            if (cookie.contains(CSRF_TOKEN)) {
                builder.addHeader(X_CSRFToken, getCrsrftoken(cookie));
                cookieString = cookieString + cleanCookies(cookie);
            } else if (cookie.contains(SESSION_ID)) {
                cookieString = cookieString + cleanCookies(cookie);
            } else {
                builder.addHeader("Cookie", cookie);
            }
//            Log.d("Cookie_Added - ", );
        }
        builder.addHeader("Cookie", cookieString);
        builder.addHeader("Referer", "https://bc166913.ngrok.io/");

        return chain.proceed(builder.build());
    }

    private String getCrsrftoken(String cookie) {
        if (cookie.contains(CSRF_TOKEN)) {
            String[] parser = cookie.split("=");
            return (parser[1].split(";"))[0];
        }

        return "";
    }

    private String cleanCookies(String cookie) {
        String[] parser = cookie.split(";");
        return parser[0] + "; ";
    }
}