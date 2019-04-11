package in.testpress.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import okhttp3.Interceptor;
import okhttp3.Response;

import static in.testpress.util.AddCookiesInterceptor.CSRF_TOKEN;
import static in.testpress.util.AddCookiesInterceptor.PREF_COOKIES;
import static in.testpress.util.AddCookiesInterceptor.SESSION_ID;

public class ReceivedCookiesInterceptor implements Interceptor {

    private Context context;

    public ReceivedCookiesInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            HashSet<String> cookies = (HashSet<String>) PreferenceManager.getDefaultSharedPreferences(context).getStringSet(PREF_COOKIES, new HashSet<String>());

            for (String header : originalResponse.headers("Set-Cookie")) {
                if (header.contains(CSRF_TOKEN)) {
                    cookies = removeLastExistingCsrfCookieSession(cookies, CSRF_TOKEN);
                }
                if (header.contains(SESSION_ID)) {
                    cookies = removeLastExistingCsrfCookieSession(cookies, SESSION_ID);
                }
                cookies.add(header);
                Log.d("Cookie_fetched", header);
            }

            SharedPreferences.Editor memes = PreferenceManager.getDefaultSharedPreferences(context).edit();
            memes.putStringSet(PREF_COOKIES, cookies).apply();
            memes.commit(); // Leave everything and first save these values.
        }

        return originalResponse;
    }

    private HashSet<String> removeLastExistingCsrfCookieSession(HashSet<String> cookies, String value) {

        Iterator<String> ite = cookies.iterator();
        try {
            while (ite.hasNext()) {
                String cookie = ite.next();
                if (cookie.contains(value)) {
                    ite.remove();
                }
            }
        } catch (Exception e){
            Log.d("error", e.getMessage());
        }
        return cookies;
    }
}
