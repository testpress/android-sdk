package in.testpress.exam.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.github.testpress.mikephil.charting.utils.Utils;

import in.testpress.exam.R;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.UIUtils;

public class  ExamsListActivity extends BaseToolBarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        UIUtils.setIndeterminateDrawable(this, progressBar, 4);
        progressBar.setVisibility(View.VISIBLE);
        CarouselFragment fragment = new CarouselFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}
