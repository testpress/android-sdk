package in.testpress.course.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import in.testpress.course.R;
import in.testpress.ui.BaseToolBarActivity;

import static in.testpress.course.TestpressCourse.COURSE_IDS;
import static in.testpress.course.TestpressCourse.PRODUCT_SLUG;


public class CoursePreviewActivity extends BaseToolBarActivity {

    private static final String TAG = "CoursePreviewActivity";

    public static Intent createIntent(ArrayList<Integer> courseIds, Context context, String productSlug) {
        Intent intent = new Intent(context, CoursePreviewActivity.class);
        intent.putExtra(COURSE_IDS, courseIds);
        intent.putExtra(PRODUCT_SLUG, productSlug);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        setContentView(R.layout.testpress_container_layout);
        CoursePreviewFragment fragment = new CoursePreviewFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }
}
