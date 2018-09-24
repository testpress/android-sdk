package in.testpress.course.ui;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.testpress.mikephil.charting.charts.PieChart;
import com.github.testpress.mikephil.charting.data.PieData;
import com.github.testpress.mikephil.charting.data.PieDataSet;
import com.github.testpress.mikephil.charting.data.PieEntry;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.Exam;
import in.testpress.util.ImageUtils;
import in.testpress.util.SingleTypeAdapter;

class ContentsListAdapter extends SingleTypeAdapter<Content> {

    private final Activity mActivity;
    private ImageLoader mImageLoader;
    private ContentDao contentDao;
    private long chapterId;

    ContentsListAdapter(Activity activity, Long chapterId) {
        super(activity, R.layout.testpress_content_list_item);
        mActivity = activity;
        mImageLoader = ImageUtils.initImageLoader(activity);
        contentDao = TestpressSDKDatabase.getContentDao(activity);
        this.chapterId = chapterId;
    }

    @Override
    public int getCount() {
        return (int) contentDao.queryBuilder()
                .where(
                        ContentDao.Properties.ChapterId.eq(chapterId),
                        ContentDao.Properties.Active.eq(true)
                ).count();
    }

    @Override
    public Content getItem(int position) {
        return contentDao.queryBuilder()
                .where(
                        ContentDao.Properties.ChapterId.eq(chapterId),
                        ContentDao.Properties.Active.eq(true)
                )
                .orderAsc(ContentDao.Properties.Order).listLazy().get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] {
                R.id.content_title, R.id.thumbnail_image, R.id.white_foreground, R.id.lock,
                R.id.content_item_layout, R.id.exam_info_layout, R.id.attempted_tick, R.id.duration,
                R.id.no_of_questions, R.id.video_completion_progress_chart,
                R.id.video_completion_progress_container
        };
    }

    @Override
    protected void update(final int position, final Content content) {
        textView(0).setTypeface(TestpressSdk.getRubikMediumFont(mActivity));
        textView(7).setTypeface(TestpressSdk.getRubikMediumFont(mActivity));
        textView(8).setTypeface(TestpressSdk.getRubikMediumFont(mActivity));
        setText(0, content.getTitle());
        // Set image
        if (content.getImage() == null || content.getImage().isEmpty()) {
            setGone(1, true);
        } else {
            setGone(1, false);
            mImageLoader.displayImage(content.getImage(), imageView(1));
        }
        Exam exam = content.getRawExam();
        // Validate lock
        if (content.getIsLocked()) {
            setGone(2, false);
            setGone(3, false);
            setGone(6, true);
            setGone(10, true);
            view(4).setClickable(false);
        } else {
            setGone(2, true);
            setGone(3, true);
            if (content.getAttemptsCount() == 0 ||
                    (content.getAttemptsCount() == 1 && exam != null &&
                            exam.getAttemptsCount() == 0 && exam.getPausedAttemptsCount() == 1)) {

                setGone(6, true);
                setGone(10, true);
            } else if (content.isNonEmbeddableVideo()) {
                displayNonEmbeddableVideoProgress(content);
            } else {
                setGone(6, false);
                setGone(10, true);
            }
            view(4).setClickable(true);
            view(4).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.startActivity(ContentActivity.createIntent(
                            content.getId(),
                            chapterId,
                            (AppCompatActivity) mActivity)
                    );
                }
            });
        }
        // Update exam info
        if (exam != null) {
            setGone(5, false);
            setText(7, exam.getDuration());
            setText(8, exam.getNumberOfQuestions().toString() + " Qs");
        } else {
            setGone(5, true);
        }
    }

    private void displayNonEmbeddableVideoProgress(Content content) {
        switch (content.getVideoWatchedPercentage()) {
            case 0:
                setGone(6, true);
                setGone(10, true);
                break;
            case 100:
                setGone(6, false);
                setGone(10, true);
                break;
            default:
                setGone(6, true);
                displayVideoWatchedPercentage(
                        (PieChart) view(9),
                        content.getVideoWatchedPercentage()
                );
                setGone(10, false);
                break;
        }
    }

    void displayVideoWatchedPercentage(PieChart chart, int percentage) {
        PieData data = getVideoProgressPieChartData(percentage);
        chart.setData(data);
        chart.setDescription("");
        chart.setClickable(false);
        chart.setTouchEnabled(false);
        chart.setUsePercentValues(true);
        chart.setCenterText(percentage + "%");
        chart.setCenterTextSize(6);
        chart.setCenterTextColor(ContextCompat.getColor(mActivity, R.color.testpress_text_gray));
        chart.setHoleRadius(85);
        chart.setTransparentCircleRadius(0);
        chart.setExtraOffsets(0, 0, 0, 0);
        chart.getLegend().setEnabled(false);
    }

    PieData getVideoProgressPieChartData(long watchedDuration) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(watchedDuration, 0));
        entries.add(new PieEntry(100 - watchedDuration, 1));

        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(mActivity, R.color.testpress_green));
        colors.add(ContextCompat.getColor(mActivity, R.color.testpress_gray_light));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);
        return data;
    }

}
