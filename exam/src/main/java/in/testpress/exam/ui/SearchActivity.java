package in.testpress.exam.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import in.testpress.exam.R;

public class SearchActivity extends AppCompatActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout_without_toolbar);
        SearchFragment searchFragment = new SearchFragment();
        searchFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, searchFragment).commitAllowingStateLoss();
    }

}
