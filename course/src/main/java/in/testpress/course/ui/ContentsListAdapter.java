package in.testpress.course.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.github.testpress.mikephil.charting.charts.PieChart;
import com.github.testpress.mikephil.charting.data.PieData;
import com.github.testpress.mikephil.charting.data.PieDataSet;
import com.github.testpress.mikephil.charting.data.PieEntry;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.models.greendao.Exam;
import in.testpress.store.ui.ProductDetailsActivity;
import in.testpress.util.ImageUtils;
import in.testpress.util.SingleTypeAdapter;

import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;

class ContentsListAdapter extends SingleTypeAdapter<Content> {

    private final Activity mActivity;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private ContentDao contentDao;
    private CourseDao courseDao;
    private ChapterDao chapterDao;
    private long chapterId;
    private String productSlug;

    ContentsListAdapter(Activity activity, Long chapterId, String productSlug) {
        super(activity, R.layout.testpress_content_list_item);
        mActivity = activity;
        mImageLoader = ImageUtils.initImageLoader(activity);
        mOptions = ImageUtils.getPlaceholdersOption();
        this.chapterId = chapterId;
        contentDao = TestpressSDKDatabase.getContentDao(activity);
        chapterDao = TestpressSDKDatabase.getChapterDao(activity);
        courseDao = TestpressSDKDatabase.getCourseDao(activity);
        this.productSlug = productSlug;
    }

    @Override
    public int getCount() {
        return (int) contentDao.queryBuilder()
                .where(
                        ContentDao.Properties.ChapterId.eq(chapterId)
                ).count();
    }

    @Override
    public Content getItem(int position) {
        return contentDao.queryBuilder()
                .where(
                        ContentDao.Properties.ChapterId.eq(chapterId)
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
                R.id.video_completion_progress_container, R.id.lock_image
        };
    }

    @Override
    protected void update(final int position, final Content content) {
        textView(0).setTypeface(TestpressSdk.getRubikMediumFont(mActivity));
        textView(7).setTypeface(TestpressSdk.getRubikMediumFont(mActivity));
        textView(8).setTypeface(TestpressSdk.getRubikMediumFont(mActivity));
        setText(0, content.getName());
        // Set image
        if (content.getImage() == null || content.getImage().isEmpty()) {
            setGone(1, true);
        } else {
            setGone(1, false);
            mImageLoader.displayImage(content.getImage(), imageView(1), mOptions);
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
                            position,
                            chapterId,
                            (AppCompatActivity) mActivity,
                            productSlug)
                    );
                }
            });
        }

        handleStoreCourseContent(content);
        // Update exam info
        if (exam != null) {
            setGone(5, false);
            setText(7, exam.getDuration());
            setText(8, exam.getNumberOfQuestions().toString() + " Qs");
        } else {
            setGone(5, true);
        }
    }

    private void handleStoreCourseContent(Content content) {
        List<Chapter> chapters = chapterDao.queryBuilder().where(ChapterDao.Properties.Id.eq(this.chapterId)).list();
        Chapter chapter = chapters.get(0);
        Course course = courseDao.queryBuilder().where(CourseDao.Properties.Id.eq(chapter.getCourseId())).list().get(0);

        if (shouldOpenPaymentPage(course, content)) {
            setGone(2, false);
            setGone(3, false);
            setGone(6, true);
            setGone(10, true);
            ((ImageView)view(11)).setImageResource(R.drawable.crown);
            view(4).setClickable(true);
            view(4).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mActivity, ProductDetailsActivity.class);
                    intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, productSlug);
                    mActivity.startActivityForResult(intent, STORE_REQUEST_CODE);
                }
            });
        }
    }


    private boolean shouldOpenPaymentPage(Course course, Content content) {
        return productSlug != null && content.getFreePreview() != null && !content.getFreePreview() && !course.getIsMyCourse();
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
