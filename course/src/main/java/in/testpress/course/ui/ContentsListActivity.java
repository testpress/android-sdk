package in.testpress.course.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;

import junit.framework.Assert;

import in.testpress.course.R;
import in.testpress.ui.BaseToolBarActivity;

import static in.testpress.course.ui.ContentActivity.FORCE_REFRESH;
import static in.testpress.course.ui.ContentActivity.GO_TO_MENU;
import static in.testpress.course.ui.ContentActivity.TESTPRESS_CONTENT_SHARED_PREFS;
import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;

public class ContentsListActivity extends BaseToolBarActivity {

    public static final String CONTENTS_URL_FRAG = "contentsUrlFrag";

    public static final String ACTIONBAR_TITLE = "title";

    private SharedPreferences prefs;

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
        String title = getIntent().getStringExtra(ACTIONBAR_TITLE);
        Assert.assertNotNull("ACTIONBAR_TITLE must not be null.", title);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(title);
        prefs = getSharedPreferences(TESTPRESS_CONTENT_SHARED_PREFS, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        loadContents();
    }

    private void loadContents() {
        ContentsListFragment fragment = new ContentsListFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TEST_TAKEN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                loadContents();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prefs.getBoolean(GO_TO_MENU, false)) {
            prefs.edit().putBoolean(GO_TO_MENU, false).apply();
            finish();
        } else if (prefs.getBoolean(FORCE_REFRESH, false)) {
            prefs.edit().putBoolean(FORCE_REFRESH, false).apply();
            loadContents();
        }
    }

}
