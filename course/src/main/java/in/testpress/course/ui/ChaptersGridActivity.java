package in.testpress.course.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import junit.framework.Assert;

import in.testpress.course.R;
import in.testpress.ui.BaseToolBarActivity;

import static in.testpress.course.ui.ChaptersGridFragment.COURSE_ID;
import static in.testpress.course.ui.ChaptersGridFragment.PARENT_ID;

public class ChaptersGridActivity extends BaseToolBarActivity {

    public static final String ACTIONBAR_TITLE = "title";

    public static Intent createIntent(String title, String courseId, String parentId,
                                      Context context) {
        Intent intent = new Intent(context, ChaptersGridActivity.class);
        intent.putExtra(ACTIONBAR_TITLE, title);
        intent.putExtra(COURSE_ID, courseId);
        intent.putExtra(PARENT_ID, parentId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        ChaptersGridFragment fragment = new ChaptersGridFragment();
        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString(ACTIONBAR_TITLE);
        Assert.assertNotNull("ACTIONBAR_TITLE must not be null.", title);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(title);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

}
