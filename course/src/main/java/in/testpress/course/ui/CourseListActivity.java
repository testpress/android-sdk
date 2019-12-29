package in.testpress.course.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import in.testpress.course.R;
import in.testpress.ui.BaseToolBarActivity;

public class CourseListActivity extends BaseToolBarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        CourseTabFragment fragment = new CourseTabFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

}
