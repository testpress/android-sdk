package in.testpress.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import in.testpress.R;

public class PermissionsUtils {

    private View rootLayout;
    private Activity activity;
    private Fragment fragment;
    private String[] permissions;
    private int permissionRequestCode;
    private PermissionRequestResultHandler resultHandler;
    private boolean checkPermission;

    public PermissionsUtils(Activity activity, View rootLayout, String[] permissions,
                            int permissionRequestCode,
                            PermissionRequestResultHandler permissionRequestResultHandler) {

        this.rootLayout = rootLayout;
        this.permissions = permissions;
        this.permissionRequestCode = permissionRequestCode;
        resultHandler = permissionRequestResultHandler;
        this.activity = activity;
    }

    public PermissionsUtils(Fragment fragment, View rootLayout, String[] permissions,
                            int permissionRequestCode,
                            PermissionRequestResultHandler permissionRequestResultHandler) {

        this(fragment.getActivity(), rootLayout, permissions, permissionRequestCode,
                permissionRequestResultHandler);

        this.fragment = fragment;
    }

    public void checkPermission() {
        if (fragment != null) {
            fragment.requestPermissions(permissions, permissionRequestCode);
        } else {
            ActivityCompat.requestPermissions(activity, permissions, permissionRequestCode);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == permissionRequestCode) {
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
            checkPermission();
        }
    }

    public interface PermissionRequestResultHandler {
        void onPermissionGranted();
    }

}
