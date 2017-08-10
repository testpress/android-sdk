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

    /**
     * Ellipsize given text if the text length exceeds the given size.
     *
     * @param text
     * @param size
     * @return String with length less than or equal to given size.
     */
    public static String ellipsize(String text, int size) {
        if (text.length() < size) {
            return text;
        } else {
            return text.substring(0, (size - 3)) + "...";
        }
    }

    public static byte getByteFromBoolean(Boolean booleanValue) {
        if (booleanValue == null) {
            return ((byte) 0);
        }
        return ((byte) (booleanValue ? 1 : 0)); // if booleanValue == true, byte == 1
    }

}
