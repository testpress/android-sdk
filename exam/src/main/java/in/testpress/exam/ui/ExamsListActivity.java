package in.testpress.exam.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;

import in.testpress.exam.R;

public class  ExamsListActivity extends BaseToolBarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout_with_tool_bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CarouselFragment fragment = new CarouselFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}
