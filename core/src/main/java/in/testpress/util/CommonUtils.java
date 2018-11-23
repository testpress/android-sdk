package in.testpress.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import in.testpress.network.RetrofitCall;

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

    public static String getUrlFrag(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.getFile().substring(1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void cancelAPIRequests(RetrofitCall[] retrofitCalls) {
        for (RetrofitCall retrofitCall : retrofitCalls) {
            if (retrofitCall != null) {
                retrofitCall.cancel();
            }
        }
    }

    public static void dismissDialogs(@NonNull Dialog[] dialogs) {
        for (Dialog dialog : dialogs) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    public static boolean isUsbConnected(Context context) {
        IntentFilter filter = new IntentFilter("android.hardware.usb.action.USB_STATE");
        Intent intent = context.registerReceiver(null, filter);
        return intent != null && intent.getExtras() != null &&
                intent.getExtras().getBoolean("connected");
    }

}
