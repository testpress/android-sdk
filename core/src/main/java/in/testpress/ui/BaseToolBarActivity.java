package in.testpress.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import in.testpress.R;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.network.RetrofitCall;
import in.testpress.util.CommonUtils;
import in.testpress.util.ImageUtils;

import static android.view.WindowManager.LayoutParams.FLAG_SECURE;
import static in.testpress.core.TestpressSdk.ACTION_PRESSED_HOME;

/**
 * Base activity used to support the toolbar & handle backpress.
 * Activity that extends this activity must needs to include the #layout/testpress_toolbar
 * in its view.
 */
public abstract class BaseToolBarActivity extends AppCompatActivity {

    public static final String ACTIONBAR_TITLE = "title";
    protected ImageView logo;
    private TestpressSession session;

    @Override
    public void setContentView(final int layoutResId) {
        session = TestpressSdk.getTestpressSession(this);
        if (session != null && session.getInstituteSettings().isScreenshotDisabled()) {
            getWindow().setFlags(FLAG_SECURE, FLAG_SECURE);
        }
        super.setContentView(layoutResId);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        logo = findViewById(R.id.toolbar_logo);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void setActionBarTitle(@StringRes int titleId) {
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(titleId);
    }

    public void setActionBarTitle(String title) {
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(title);
    }

    public void setLogo() {
        getSupportActionBar().setTitle("");
        if (session == null || session.getInstituteSettings() == null) {
            return;
        }
        String url = session.getInstituteSettings().getAppToolbarLogo();
        ImageLoader imageLoader = ImageUtils.initImageLoader(this);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        imageLoader.displayImage(url, logo, options);
    }

    @SuppressWarnings("DanglingJavadoc")
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            /**
             * Set result with home button pressed flag if activity is started by
             * {@link #startActivityForResult}
             *
             * Note: {@link #getCallingActivity} return null if activity is started using
             * {@link #startActivity} instead of {@link #startActivityForResult}
             */
            if (getCallingActivity() == null) {
                onBackPressed();
            } else {
                setResult(RESULT_CANCELED, new Intent().putExtras(getDataToSetResult()));
                super.onBackPressed();
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("DanglingJavadoc")
    @Override
    public void onBackPressed() {
        /**
         * Set result with home button pressed flag false if activity is started by
         * {@link #startActivityForResult}
         */
        if (getCallingActivity() != null) {
            setResult(RESULT_CANCELED, new Intent().putExtra(ACTION_PRESSED_HOME, false));
        }
        try {
            super.onBackPressed();
        } catch (IllegalStateException e) {
            supportFinishAfterTransition();
        }
    }

    protected Bundle getDataToSetResult() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ACTION_PRESSED_HOME, true);
        return bundle;
    }

    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] {};
    }

    @Override
    protected void onDestroy() {
        CommonUtils.cancelAPIRequests(getRetrofitCalls());
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        TestpressSession session = TestpressSdk.getTestpressSession(this);
        if (session != null && session.getInstituteSettings().isScreenshotDisabled()) {
            getWindow().setFlags(FLAG_SECURE, FLAG_SECURE);
        }
    }

}
