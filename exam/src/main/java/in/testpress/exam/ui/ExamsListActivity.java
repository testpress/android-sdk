package in.testpress.exam.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import in.testpress.exam.R;
import in.testpress.ui.BaseToolBarActivity;

public class  ExamsListActivity extends BaseToolBarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        CarouselFragment fragment = new CarouselFragment();
        //Log.e("Calling Activity",getCallingActivity().getClassName()+"");
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}
