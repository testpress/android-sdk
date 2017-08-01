package in.testpress.course.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;

import in.testpress.course.R;
import in.testpress.ui.BaseToolBarActivity;

public class LeaderboardActivity extends BaseToolBarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        LeaderboardFragment.show(this, R.id.fragment_container);
    }

}
