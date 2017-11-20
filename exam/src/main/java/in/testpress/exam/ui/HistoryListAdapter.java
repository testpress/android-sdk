package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.Date;
import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.TestpressExam;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.ExamDao;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ViewUtils;

import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;
import static in.testpress.exam.ui.TestActivity.PARAM_EXAM;
import static in.testpress.models.greendao.ExamDao.*;

public class HistoryListAdapter extends SingleTypeAdapter<Exam> {

    private final Activity activity;
    private final Fragment fragment;
    private ExamDao examDao;

    /**
     * @param fragment
     * @param items
     */
    public HistoryListAdapter(final Fragment fragment, final List<Exam> items, ExamDao examDao) {
        super(fragment.getActivity().getLayoutInflater(), R.layout.testpress_content_list_item);
        this.activity = fragment.getActivity();
        this.fragment = fragment;
        this.examDao = examDao;
        setItems(items);
    }


    @Override
    public int getCount() {
        Date today = new Date();
        QueryBuilder<Exam> queryBuilder = examDao.queryBuilder();
        return (int) queryBuilder.whereOr(
                Properties.AttemptsCount.notEq("0"),
                Properties.PausedAttemptsCount.notEq("0"),
                Properties.EndDate.gt(today)
        ).count();
    }

    @Override
    public Exam getItem(int position) {
        Date today = new Date();
        return examDao.queryBuilder()
                .whereOr(
                        Properties.AttemptsCount.notEq("0"),
                        Properties.PausedAttemptsCount.notEq("0"),
                        Properties.EndDate.gt(today)
                ).orderAsc(Properties.StartDate).listLazy().get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.content_title, R.id.white_foreground, R.id.lock,
                R.id.content_item_layout, R.id.exam_info_layout, R.id.attempted_tick,
                R.id.duration, R.id.no_of_questions, R.id.comment_count_layout, R.id.no_of_comments };
    }

    @Override
    protected void update(final int position, final Exam exam) {
        ViewUtils.setTypeface(new TextView[] { textView(0), textView(6), textView(7), textView(9) },
                TestpressSdk.getRubikMediumFont(activity));

        setText(0, exam.getTitle());
        setText(6, exam.getDuration());
        setText(7, exam.getNumberOfQuestions().toString() + " Qs");
        setGone(1, true);
        setGone(2, true);
        setGone(4, false);
        setGone(5, true);
        view(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, AttemptsActivity.class);
                intent.putExtra(PARAM_EXAM, exam.getId());
                intent.putExtra(TestpressExam.PARAM_EXAM_SLUG, exam.getSlug());
                fragment.startActivityForResult(intent, TEST_TAKEN_REQUEST_CODE);
            }
        });
        if (exam.getCommentsCount() == 0) {
            setGone(8, true);
        } else {
            setText(9, exam.getCommentsCount().toString());
            setGone(8, false);
        }
    }

}
