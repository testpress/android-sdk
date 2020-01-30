package in.testpress.exam.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import in.testpress.exam.R;
import in.testpress.models.greendao.Exam;
import in.testpress.ui.BaseToolBarActivity;

import static in.testpress.exam.ui.AccessCodeExamsFragment.ACCESS_CODE;
import static in.testpress.exam.ui.AccessCodeExamsFragment.EXAMS;

public class AccessCodeExamsActivity extends BaseToolBarActivity {

    public static Intent getIntent(Context context, String accessCode, List<Exam> exams) {

        Intent intent = new Intent(context, AccessCodeExamsActivity.class);
        intent.putExtra(ACCESS_CODE, accessCode);
        ArrayList<Exam> examsList = new ArrayList<>(exams);
        intent.putParcelableArrayListExtra(EXAMS, examsList);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        AccessCodeExamsFragment fragment = new AccessCodeExamsFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

}
