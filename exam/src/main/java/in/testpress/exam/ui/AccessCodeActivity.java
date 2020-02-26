package in.testpress.exam.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;

import in.testpress.exam.R;
import in.testpress.ui.BaseToolBarActivity;

public class AccessCodeActivity extends BaseToolBarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        AccessCodeFragment.show(this, R.id.fragment_container);
    }

}
