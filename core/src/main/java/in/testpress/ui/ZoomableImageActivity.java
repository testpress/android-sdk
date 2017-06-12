package in.testpress.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import in.testpress.R;
import in.testpress.ui.view.TouchImageView;
import in.testpress.util.ImageUtils;
import in.testpress.util.UIUtils;

public class ZoomableImageActivity extends Activity {

    public static final String IMAGE_URL = "imageUrl";
    ProgressBar progressBar;

    public static Intent createIntent(String imageUrl, Context context) {
        Intent intent = new Intent(context, ZoomableImageActivity.class);
        intent.putExtra(IMAGE_URL, imageUrl);
        return intent;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.testpress_activity_zoomable_image);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        UIUtils.setIndeterminateDrawable(this, progressBar, 4);
        findViewById(R.id.image).setBackgroundColor(Color.BLACK);
        final TextView emptyView = (TextView)findViewById(R.id.empty);
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .build();
        ImageUtils.initImageLoader(this).loadImage(getIntent().getStringExtra(IMAGE_URL), options,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        progressBar.setVisibility(View.GONE);
                        if(failReason.getType().equals(FailReason.FailType.IO_ERROR)) {
                            emptyView.setVisibility(View.VISIBLE);
                            emptyView.setText(R.string.testpress_no_internet_try_again);
                        } else {
                            emptyView.setVisibility(View.VISIBLE);
                            emptyView.setText(getString(R.string.testpress_some_thing_went_wrong_try_again));
                        }
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        progressBar.setVisibility(View.GONE);
                        ((TouchImageView) findViewById(R.id.image)).setImageBitmap(loadedImage);
                    }
                });
    }
}
