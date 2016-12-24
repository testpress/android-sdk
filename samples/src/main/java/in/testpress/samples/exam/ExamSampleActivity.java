package in.testpress.samples.exam;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import in.testpress.core.TestpressSdk;
import in.testpress.exam.TestpressExam;
import in.testpress.samples.BaseToolBarActivity;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;

import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;
import static in.testpress.samples.core.TestpressCoreSampleActivity.AUTHENTICATE_REQUEST_CODE;

public class ExamSampleActivity extends BaseToolBarActivity {

    private int selectedItem;
    private String examSlug;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_exams_as);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getExamSlug();
            }
        });
        findViewById(R.id.exam_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSDK(view.getId());
            }
        });
        findViewById(R.id.categories).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSDK(view.getId());
            }
        });
        findViewById(R.id.fragment_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExamSampleActivity.this, NavigationDrawerActivity.class);
                startActivity(intent);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void showSDK(int id) {
        selectedItem = id;
        if (TestpressSdk.hasActiveSession(this)) {
            if (id == R.id.exam) {
                TestpressExam.startExam(this, examSlug, TestpressSdk.getTestpressSession(this));
            } else if (id == R.id.exam_list) {
                TestpressExam.show(this, TestpressSdk.getTestpressSession(this));
            } else {
                TestpressExam.showCategories(this, TestpressSdk.getTestpressSession(this));
            }
        } else {
            Intent intent = new Intent(this, TestpressCoreSampleActivity.class);
            startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHENTICATE_REQUEST_CODE && resultCode == RESULT_OK) {
            showSDK(selectedItem);
        } else if (requestCode == TEST_TAKEN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "User attempted the exam", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                if (data.getBooleanExtra(TestpressExam.ACTION_PRESSED_HOME, false)) {
                    Toast.makeText(this, "User pressed home button", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "User pressed back button", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    void getExamSlug() {
        final View dialog = getLayoutInflater().inflate(R.layout.edit_text_dialog_box, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.TestpressAppCompatAlertDialogStyle);
        builder.setTitle("Enter exam slug");
        builder.setView(dialog);
        final EditText editText = (EditText) dialog.findViewById(R.id.edit_text);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                examSlug = editText.getText().toString();
                if (examSlug.trim().isEmpty()) {
                    return;
                }
                showSDK(R.id.exam);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

}
