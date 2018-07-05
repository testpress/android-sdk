package in.testpress.exam.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;

import com.theartofdev.edmodo.cropper.CropImage;

import in.testpress.util.PermissionsUtils;

import static android.app.Activity.RESULT_OK;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE;

public class ImagePickerUtils {

    private static final String[] IMAGE_PICKER_PERMISSIONS = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private View rootLayout;
    private Activity activity;
    private Fragment fragment;
    private Uri selectedImageUri;
    public PermissionsUtils permissionsUtils;
    private int[] aspectRatio = null;

    private PermissionsUtils.PermissionRequestResultHandler permissionRequestResultHandler =
            new PermissionsUtils.PermissionRequestResultHandler() {
                @Override
                public void onPermissionGranted() {
                    startCropImageActivity(selectedImageUri);
                }
            };

    public ImagePickerUtils(View rootLayout, Activity activity) {
        this.rootLayout = rootLayout;
        this.activity = activity;
        permissionsUtils = new PermissionsUtils(activity, rootLayout, IMAGE_PICKER_PERMISSIONS,
                PICK_IMAGE_PERMISSIONS_REQUEST_CODE, permissionRequestResultHandler);

    }

    public ImagePickerUtils(View rootLayout, Fragment fragment) {
        this.rootLayout = rootLayout;
        this.fragment = fragment;
        this.activity = fragment.getActivity();
        permissionsUtils = new PermissionsUtils(fragment, rootLayout, IMAGE_PICKER_PERMISSIONS,
                PICK_IMAGE_PERMISSIONS_REQUEST_CODE, permissionRequestResultHandler);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data,
                                 ImagePickerResultHandler handler) {

        if (requestCode == PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedImageUri = CropImage.getPickImageResultUri(activity, data);
            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (permissionsUtils.checkPermissionRequired() &&
                    CropImage.isUriRequiresPermissions(activity, selectedImageUri)) {

                permissionsUtils.requestPermissions();
            } else {
                // No permissions required or already granted
                startCropImageActivity(selectedImageUri);
            }
        } else if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                handler.onSuccessfullyImageCropped(result);
            } else if (resultCode == CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //noinspection ThrowableResultOfMethodCallIgnored
                Exception exception = result.getError();
                Snackbar.make(rootLayout, exception.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.ActivityBuilder activityBuilder = getActivityBuilder(imageUri);
        if (aspectRatio != null) {
            activityBuilder.setAspectRatio(aspectRatio[0], aspectRatio[1]);
        }
        if (fragment == null) {
            activityBuilder.start(activity);
        } else {
            activityBuilder.start(activity, fragment);
        }
    }

    protected CropImage.ActivityBuilder getActivityBuilder(Uri imageUri) {
        return CropImage.activity(imageUri)
                .setBorderCornerThickness(0)
                .setAllowFlipping(false);
    }

    public void setImagePickerPermissions(String[] permissions) {
        permissionsUtils.setPermissions(permissions);
    }

    public void setAspectRatio(int aspectRatioX, int aspectRatioY) {
        aspectRatio = new int[] { aspectRatioX, aspectRatioY };
    }

    public interface ImagePickerResultHandler {
        void onSuccessfullyImageCropped(CropImage.ActivityResult result);
    }

}
