package in.testpress.exam.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import android.view.View;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import in.testpress.exam.R;
import in.testpress.util.PermissionsUtils;
import in.testpress.util.ViewUtils;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE;

public class ImageUtils {

    private static final String[] IMAGE_PERMISSIONS = new String[] {
            READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE
    };

    private View rootLayout;
    private Activity activity;
    private Fragment fragment;
    private Uri selectedImageUri;
    public PermissionsUtils permissionsUtils;
    private int[] aspectRatio = null;

    public ImageUtils(View rootLayout, Activity activity) {
        this.rootLayout = rootLayout;
        this.activity = activity;
        permissionsUtils = new PermissionsUtils(activity, rootLayout, IMAGE_PERMISSIONS);

    }

    public ImageUtils(View rootLayout, Fragment fragment) {
        this.rootLayout = rootLayout;
        this.fragment = fragment;
        this.activity = fragment.getActivity();
        permissionsUtils = new PermissionsUtils(fragment, rootLayout, IMAGE_PERMISSIONS);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data,
                                 ImagePickerResultHandler handler) {

        if (requestCode == PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedImageUri = CropImage.getPickImageResultUri(activity, data);
            permissionsUtils.checkPermissionRequired(
                    selectedImageUri,
                    new PermissionsUtils.PermissionRequestResultHandler() {
                        @Override
                        public void onPermissionGranted() {
                            startCropImageActivity(selectedImageUri);
                        }
                    });
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

    public static Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return bitmap;
    }

    public static void shareBitmap(Bitmap bitmap, Context context) {
        File image = new File(context.getCacheDir() , "screenshot.png");
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.flush();
            outputStream.close();
            shareImage(image, context);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void shareImage(File file, Context context) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        String shareMessage = String.format(
                context.getString(R.string.testpress_share_screenshot_text),
                context.getApplicationInfo().loadLabel(context.getPackageManager()),
                context.getPackageName()
        );
        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(Intent.createChooser(intent,
                    context.getString(R.string.testpress_share_screenshot)));

        } catch (ActivityNotFoundException e) {
            ViewUtils.toast(context, context.getString(R.string.testpress_no_app_available));
        }
    }

    public interface ImagePickerResultHandler {
        void onSuccessfullyImageCropped(CropImage.ActivityResult result);
    }

}
