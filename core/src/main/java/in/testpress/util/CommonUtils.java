package in.testpress.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import in.testpress.models.greendao.Content;
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

    public static boolean isAppInstalled(String uri, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        }
        catch (PackageManager.NameNotFoundException ignore) {}
        return false;
    }

    public static String getUserName(Context context) {
        AccountManager manager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] account = manager.getAccountsByType(context.getPackageName());
        if (account.length > 0) {
            return account[0].name;
        }

        return "";
    }

    public static String[] getUserCredentials(Context context) {
        AccountManager manager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = manager.getAccountsByType(context.getPackageName());

        if (accounts.length > 0) {
            String username = accounts[0].name;
            String password = manager.getPassword(accounts[0]);
            return new String[]{username, password};
        }

        return new String[]{"", ""};
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
