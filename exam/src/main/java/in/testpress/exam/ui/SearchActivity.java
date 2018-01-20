package in.testpress.exam.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import in.testpress.exam.R;

public class SearchActivity extends AppCompatActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout_without_toolbar);
        SearchFragment searchFragment = new SearchFragment();
        searchFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, searchFragment).commitAllowingStateLoss();
    }

}
