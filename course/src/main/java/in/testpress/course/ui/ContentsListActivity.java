package in.testpress.course.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import junit.framework.Assert;

import in.testpress.course.R;
import in.testpress.ui.BaseToolBarActivity;

public class ContentsListActivity extends BaseToolBarActivity {

    public static final String CONTENTS_URL_FRAG = "contentsUrlFrag";

    public static final String ACTIONBAR_TITLE = "title";

    public static Intent createIntent(String title, String chaptersUrlFrag, Context context) {
        Intent intent = new Intent(context, ContentsListActivity.class);
        intent.putExtra(ACTIONBAR_TITLE, title);
        intent.putExtra(CONTENTS_URL_FRAG, chaptersUrlFrag);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        ContentsListFragment fragment = new ContentsListFragment();
        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString(ACTIONBAR_TITLE);
        Assert.assertNotNull("ACTIONBAR_TITLE must not be null.", title);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(title);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commit();
    }

}
