package in.testpress.ui;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import in.testpress.R;
import in.testpress.util.FontUtils;

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
        FontUtils.applyTestpressFont(getApplicationContext(), toolbar, true);
        //noinspection ConstantConditions
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }
}
