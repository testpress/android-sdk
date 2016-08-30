package in.testpress.exam.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.Exam;
import in.testpress.exam.util.SingleTypeAdapter;

class AttemptsListAdapter extends SingleTypeAdapter<Attempt> {

    private Activity activity;
    private Exam exam;

    public AttemptsListAdapter(final Activity activity, final List<Attempt> items, final Exam exam,
                               int layout) {
        super(activity.getLayoutInflater(), layout);
        this.activity = activity;
        this.exam = exam;
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] {
                R.id.completed_attempt, R.id.paused_attempt, R.id.attempt_date,
                R.id.percentile, R.id.correct_count, R.id.incorrect_count, R.id.score,
                R.id.paused_attempt_date, R.id.remaining_time
        };
    }

    @Override
    protected void update(final int position, final Attempt item) {
        if (item.getState().equals("Running")) {
            updater.view.findViewById(R.id.completed_attempt).setVisibility(View.GONE);
            updater.view.findViewById(R.id.paused_attempt).setVisibility(View.VISIBLE);
            setText(7, getStringFromResource(activity, R.string.testpress_started_on) + " " + item.getDate());
            setText(8, item.getRemainingTime());
        } else {
            updater.view.findViewById(R.id.paused_attempt).setVisibility(View.GONE);
            updater.view.findViewById(R.id.completed_attempt).setVisibility(View.VISIBLE);
            setText(2, getStringFromResource(activity, R.string.testpress_attempted_on) + " " + item.getDate());
            setText(3, item.getPercentile());
            setText(4, "" + item.getCorrectCount());
            setText(5, "" + item.getIncorrectCount());
            setText(6, "" + item.getScore());
        }
        Button reviewButton = (Button) updater.view.findViewById(R.id.review_attempt);
        setLeftDrawable(reviewButton, R.drawable.ic_zoom_in_white_18dp);
        reviewButton.setOnClickListener(new View.OnClickListener
                () {
            @Override
            public void onClick(View v) {
                activity.startActivity(ReviewActivity.createIntent(activity, exam, item));
            }
        });
        Button endButton = (Button) updater.view.findViewById(R.id.end);
        setLeftDrawable(endButton, R.drawable.ic_block_white_18dp);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(activity, R.style.TestpressAppCompatAlertDialogStyle)
                        .setTitle(R.string.testpress_end_message)
                        .setPositiveButton(R.string.testpress_end, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(activity, TestActivity.class);
                                intent.putExtra(TestActivity.PARAM_EXAM, exam);
                                intent.putExtra(TestActivity.PARAM_ATTEMPT, item);
                                intent.putExtra(TestActivity.PARAM_ACTION,
                                        TestActivity.PARAM_VALUE_ACTION_END);
                                activity.startActivityForResult(intent,
                                        CarouselFragment.TEST_TAKEN_REQUEST_CODE);
                            }
                        })
                        .setNegativeButton(R.string.testpress_cancel, null)
                        .show();
            }
        });
        Button resumeButton = (Button) updater.view.findViewById(R.id.resume_exam);
        setLeftDrawable(resumeButton, R.drawable.ic_repeat_white_18dp);
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, TestActivity.class);
                intent.putExtra(TestActivity.PARAM_EXAM, exam);
                intent.putExtra(TestActivity.PARAM_ATTEMPT, item);
                activity.startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
            }
        });
        Button emailPdf = (Button) updater.view.findViewById(R.id.email_pdf);
        setLeftDrawable(emailPdf, R.drawable.ic_email_white_18dp);
        emailPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EmailPdfDialog(activity, R.style.TestpressAppCompatAlertDialogStyle, true,
                        item.getUrlFrag()).show();
            }
        });
        if (exam.getAllowPdf()) {
            emailPdf.setVisibility(View.VISIBLE);
        } else {
            emailPdf.setVisibility(View.GONE);
        }
    }

    public void setLeftDrawable(Button button, @DrawableRes int drawableRes) {
        Drawable drawable = activity.getResources().getDrawable(drawableRes);
        drawable.setColorFilter(new PorterDuffColorFilter(activity.getResources().getColor(
                R.color.testpress_button_text_color), PorterDuff.Mode.MULTIPLY));
        button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        button.setCompoundDrawablePadding((int) activity.getResources().getDimension(
                R.dimen.testpress_button_left_drawable_padding));
    }
}
