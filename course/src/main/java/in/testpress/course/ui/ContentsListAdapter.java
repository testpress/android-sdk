package in.testpress.course.ui;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.Exam;
import in.testpress.util.ImageUtils;
import in.testpress.util.SingleTypeAdapter;

import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;

class ContentsListAdapter extends SingleTypeAdapter<Content> {

    private final Activity mActivity;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private ContentDao contentDao;
    private long chapterId;

    ContentsListAdapter(Activity activity, final List<Content> items, int layout,
                        ContentDao contentDao, Long chapterId) {

        super(activity.getLayoutInflater(), layout);
        mActivity = activity;
        mImageLoader = ImageUtils.initImageLoader(activity);
        mOptions = ImageUtils.getPlaceholdersOption();
        this.contentDao = contentDao;
        this.chapterId = chapterId;
        setItems(items);
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
        return new int[] { R.id.content_title, R.id.thumbnail_image, R.id.white_foreground,
                R.id.lock, R.id.content_item_layout, R.id.exam_info_layout, R.id.attempted_tick,
                R.id.duration, R.id.no_of_questions };
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
        if (content.getIsLocked() || !content.getHasStarted()) {
            setGone(2, false);
            setGone(3, false);
            setGone(6, true);
            view(4).setClickable(false);
        } else {
            setGone(2, true);
            setGone(3, true);
            if (content.getAttemptsCount() == 0 ||
                    (content.getAttemptsCount() == 1 && exam != null &&
                            exam.getPausedAttemptsCount() > 0)) {
                setGone(6, true);
            } else {
                setGone(6, false);
            }
            view(4).setClickable(true);
            view(4).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.startActivityForResult(
                            ContentActivity.createIntent(
                                    position,
                                    chapterId,
                                    (AppCompatActivity) mActivity
                            ),
                            TEST_TAKEN_REQUEST_CODE
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

}
