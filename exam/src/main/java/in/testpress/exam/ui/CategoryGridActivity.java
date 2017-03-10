package in.testpress.exam.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import in.testpress.exam.R;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.ViewUtils;

import static in.testpress.exam.ui.CategoriesGridFragment.PARENT_ID;
import static in.testpress.exam.ui.ExamsListFragment.CATEGORY;

public class CategoryGridActivity extends BaseToolBarActivity {

    public static final String SHOW_EXAMS_AS_DEFAULT = "showExamsAsDefault";
    public static final String ACTIONBAR_TITLE = "title";

    private Fragment currentFragment;

    public static Intent createIntent(String title, String parentId, String parentSlug,
                                      Context context) {
        Intent intent = new Intent(context, CategoryGridActivity.class);
        intent.putExtra(ACTIONBAR_TITLE, title);
        intent.putExtra(PARENT_ID, parentId);
        intent.putExtra(CATEGORY, parentSlug);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        String title = getIntent().getStringExtra(ACTIONBAR_TITLE);
        if (title != null) {
            //noinspection ConstantConditions
            getSupportActionBar().setTitle(title);
        }
        if (getIntent().getBooleanExtra(SHOW_EXAMS_AS_DEFAULT, false)) {
            currentFragment = new CarouselFragment();
        } else {
            currentFragment = new CategoriesGridFragment();
        }
        displayCurrentFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.testpress_list_grid, menu);
        MenuItem list = menu.findItem(R.id.list);
        MenuItem grid = menu.findItem(R.id.grid);
        ViewUtils.setMenuIconsColor(this, new MenuItem[] {list, grid});
        if (currentFragment != null) {
            if (currentFragment instanceof CategoriesGridFragment) {
                list.setVisible(true);
                grid.setVisible(false);
            } else {
                list.setVisible(false);
                grid.setVisible(true);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (currentFragment == null) {
            return false;
        }
        if (R.id.list == item.getItemId()) {
            currentFragment = new CarouselFragment();
            displayCurrentFragment();
            return true;
        } else if (R.id.grid == item.getItemId()) {
            currentFragment = new CategoriesGridFragment();
            displayCurrentFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayCurrentFragment() {
        currentFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, currentFragment)
                .commitAllowingStateLoss();
    }

}
