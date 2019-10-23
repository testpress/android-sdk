package in.testpress.exam.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;

import in.testpress.exam.R;
import in.testpress.ui.BaseToolBarActivity;

public class  ExamsListActivity extends BaseToolBarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        CarouselFragment fragment = new CarouselFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

}
