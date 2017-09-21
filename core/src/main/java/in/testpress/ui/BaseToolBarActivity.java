package in.testpress.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import in.testpress.R;

import static in.testpress.core.TestpressSdk.ACTION_PRESSED_HOME;

/**
 * Base activity used to support the toolbar & handle backpress.
 * Activity that extends this activity must needs to include the #layout/testpress_toolbar
 * in its view.
 */
public abstract class BaseToolBarActivity extends AppCompatActivity {

    @Override
    public void setContentView(final int layoutResId) {
        super.setContentView(layoutResId);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        super.onBackPressed();
    }

    protected Bundle getDataToSetResult() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ACTION_PRESSED_HOME, true);
        return bundle;
    }

}
