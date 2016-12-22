package in.testpress.exam.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import in.testpress.exam.R;
import in.testpress.ui.BaseToolBarActivity;

import static in.testpress.exam.ui.CategoriesGridFragment.PARENT_ID;

public class CategoryGridActivity extends BaseToolBarActivity {

    public static final String ACTIONBAR_TITLE = "title";

    public static Intent createIntent(String title, String parentId, Context context) {
        Intent intent = new Intent(context, CategoryGridActivity.class);
        intent.putExtra(ACTIONBAR_TITLE, title);
        intent.putExtra(PARENT_ID, parentId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        CategoriesGridFragment fragment = new CategoriesGridFragment();
        String title = getIntent().getStringExtra(ACTIONBAR_TITLE);
        if (title != null) {
            //noinspection ConstantConditions
            getSupportActionBar().setTitle(title);
        }
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commit();
    }

}
