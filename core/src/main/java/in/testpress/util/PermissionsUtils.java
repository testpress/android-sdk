package in.testpress.util;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.View;

import java.io.InputStream;

import in.testpress.R;

import static in.testpress.core.TestpressSdk.PERMISSIONS_REQUEST_CODE;

public class PermissionsUtils {

    private View rootLayout;
    private Activity activity;
    private Fragment fragment;
    private String[] permissions;
    private PermissionRequestResultHandler resultHandler;
    private boolean checkPermission;

    public PermissionsUtils(Activity activity, View rootLayout, String[] permissions) {
        this.rootLayout = rootLayout;
        this.permissions = permissions;
        this.activity = activity;
    }

    public PermissionsUtils(Fragment fragment, View rootLayout, String[] permissions) {
        this(fragment.getActivity(), rootLayout, permissions);
        this.fragment = fragment;
    }

    public PermissionsUtils(Activity activity, View rootLayout) {
        this.activity = activity;
        this.rootLayout = rootLayout;
    }

    public boolean isStoragePermissionGranted(){
        return ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestStoragePermissionWithSnackbar() {
        Snackbar.make(
                rootLayout,
                "Required Storage Permission",
                Snackbar.LENGTH_LONG
        ).setAction("Allow", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            }
        }).show();
    }

    public boolean checkPermissionRequired() {
        return checkPermissionRequired(activity, permissions);
    }

    public static boolean checkPermissionRequired(Context context, String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                boolean required =
                        context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED;

                if (required) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkPermissionRequired(Uri uri) {
        return checkPermissionRequired() && isUriRequiresPermissions(uri);
    }

    public void checkPermissionRequired(Uri uri, PermissionRequestResultHandler resultHandler) {
        if (checkPermissionRequired(uri)) {
            requestPermissions(resultHandler);
        } else {
            resultHandler.onPermissionGranted();
        }
    }

    public void requestPermissions(PermissionRequestResultHandler resultHandler) {
        this.resultHandler = resultHandler;
        if (fragment != null) {
            fragment.requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(activity, permissions, PERMISSIONS_REQUEST_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                resultHandler.onPermissionGranted();
            } else {
                boolean show =
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0]);

                if (!show) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity,
                            R.style.TestpressAppCompatAlertDialogStyle);

                    builder.setTitle(R.string.testpress_permission_denied);
                    builder.setMessage(R.string.testpress_permission_denied_message);
                    builder.setPositiveButton(R.string.testpress_go_to_settings,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    checkPermission = true;
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                                    intent.setData(uri);
                                    activity.startActivity(intent);
                                }
                    });
                    builder.setNegativeButton(R.string.testpress_deny, null);
                    builder.show();
                } else {
                    Snackbar.make(rootLayout, R.string.testpress_action_cant_done_without_permission,
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void onResume() {
        if (checkPermission) {
            checkPermission = false;
            requestPermissions(resultHandler);
        }
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    /**
     * Test if we can open the given Android URI to test if permission required error is thrown.<br>
     * Only relevant for API version 23 and above.
     * https://github.com/ArthurHub/Android-Image-Cropper/blob/2.8.0/cropper/src/main/java/com/theartofdev/edmodo/cropper/CropImage.java#L396
     *
     * @param uri the result URI of image pick.
     */
    public boolean isUriRequiresPermissions(@NonNull Uri uri) {
        try {
            ContentResolver resolver = activity.getContentResolver();
            InputStream stream = resolver.openInputStream(uri);
            if (stream != null) {
                stream.close();
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public interface PermissionRequestResultHandler {
        void onPermissionGranted();
    }

}
