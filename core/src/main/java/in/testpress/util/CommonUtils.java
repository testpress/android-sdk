package in.testpress.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class CommonUtils {

    public static String getStringFromAsset(Context context, String path) {
        try {
            InputStream is = context.getAssets().open(path);
            int size = is.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();
            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
