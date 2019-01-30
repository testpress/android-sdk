package in.testpress.course.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.network.ChapterPager;
import in.testpress.course.network.ContentPager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.course.util.StringUtil;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseCredit;
import in.testpress.models.greendao.CourseCreditDao;
import in.testpress.models.greendao.CourseDao;
import in.testpress.network.BaseResourcePager;
import in.testpress.ui.view.ReadMoreTextView;
import in.testpress.util.ImageUtils;
import in.testpress.util.SingleTypeAdapter;

import static android.app.ProgressDialog.STYLE_HORIZONTAL;
import static android.content.DialogInterface.BUTTON_NEGATIVE;

class CourseListAdapter extends SingleTypeAdapter<Course> {

    private final Activity mActivity;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private CourseDao mCourseDao;
    private TestpressCourseApiClient apiClient;
    private BaseResourcePager currentPager;
    private ProgressDialog progressDialog;
    private AlertDialog wantToCancelDialog;
    private AlertDialog retryDialog;

    CourseListAdapter(Activity activity, CourseDao courseDao) {
        super(activity.getLayoutInflater(), R.layout.testpress_course_list_item);
        mActivity = activity;
        mImageLoader = ImageUtils.initImageLoader(activity);
        mOptions = ImageUtils.getPlaceholdersOption();
        mCourseDao = courseDao;
        apiClient = new TestpressCourseApiClient(activity);
        progressDialog = new ProgressDialog(activity, R.style.TestpressAppCompatAlertDialogStyle);
        progressDialog.setMessage(activity.getString(R.string.testpress_fetching_contents));
        progressDialog.setProgressStyle(STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        wantToCancelDialog = new AlertDialog.Builder(mActivity, R.style.TestpressAppCompatAlertDialogStyle)
                .setTitle(R.string.testpress_are_you_sure)
                .setMessage(R.string.testpress_want_to_cancel)
                .setPositiveButton(R.string.testpress_yes, null)
                .create();
    }

    @Override
    public int getCount() {
        return (int) mCourseDao.queryBuilder().count();
    }

    @Override
    public Course getItem(int position) {
        return mCourseDao.queryBuilder()
                .orderAsc(CourseDao.Properties.Order)
                .listLazy().get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.course_title, R.id.thumbnail_image, R.id.percentage,
                R.id.course_item_layout, R.id.progress_bar_layout, R.id.course_description,
                R.id.course_content_detail};
    }

    @Override
    protected void update(final int position, final Course course) {
        setFont(new int[]{0, 2}, TestpressSdk.getRubikMediumFont(mActivity));
        setText(0, course.getTitle());
        if (course.getDescription() == null || course.getDescription().isEmpty()) {
            setGone(5, true);
        } else {
            ReadMoreTextView descriptionView = (ReadMoreTextView) textView(5);
            descriptionView.setText(course.getDescription(), true);
            textView(5).setTypeface(TestpressSdk.getRubikRegularFont(mActivity));
            setGone(5, false);
        }
        if (course.getImage() == null || course.getImage().isEmpty()) {
            setGone(1, true);
        } else {
            setGone(1, false);
            mImageLoader.displayImage(course.getImage(), imageView(1), mOptions);
        }
        view(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (course.getChildItemsLoaded() || course.getChaptersCount() == 0) {
                    displayChapters(course);
                } else {
                    showProgressDialog(course);
                }
            }
        });

        LinearLayout linearLayout = view(6);

        // Add total numbers of chapters
        TextView chapterCountTextView = (TextView) linearLayout.findViewById(R.id.total_chapters);
        TextView chapterDetailTextView = (TextView) linearLayout.findViewById(R.id.chapter_detail_text);

        chapterCountTextView.setText(course.getChaptersCount().toString());
        chapterDetailTextView.setText(StringUtil.getPluralString(
                mActivity,
                R.plurals.testpress_chapter_plural_name,
                course.getChaptersCount(),
                "Chapter"
        ));

        // Add total numbers of contents
        TextView totalContentTextView = linearLayout.findViewById(R.id.total_contents);
        TextView contentDetailTextView = (TextView) linearLayout.findViewById(R.id.content_detail_text);

        totalContentTextView.setText(course.getContentsCount().toString());
        contentDetailTextView.setText(StringUtil.getPluralString(
                mActivity,
                R.plurals.testpress_content_plural_name,
                course.getContentsCount(),
                "Content"
        ));

        // Add percentage to course completion progress
        CourseCredit courseCredit = getCourseCreditByCourseId(course.getId());
        LinearLayout progressBarLinearLayout = view(4);
        int progress = calculateCourseProgressPercentage(courseCredit, course.getContentsCount());

        ((ProgressBar)progressBarLinearLayout.findViewById(R.id.progress_bar)).setProgress(progress);
        ((TextView) progressBarLinearLayout.findViewById(R.id.percentage))
                .setText(String.format(mActivity.getResources().getString(R.string.testpress_percentage), progress));
    }



    private void showProgressDialog(final Course course) {
        int itemsCount = course.getChaptersCount() + course.getContentsCount();
        progressDialog.setMax(itemsCount);
        wantToCancelDialog.setButton(BUTTON_NEGATIVE, mActivity.getString(R.string.testpress_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!progressDialog.isShowing()) {
                            progressDialog.show();
                        }
                        restartLoading(course);
                    }
                });
        progressDialog.setButton(BUTTON_NEGATIVE, mActivity.getString(R.string.testpress_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        wantToCancelDialog.show();
                    }
                });
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                currentPager.cancelAsyncRequest();
            }
        });
        progressDialog.show();
        progressDialog.setProgress(0);
        fetchChapters(course, new ChapterPager(course.getId(), apiClient));
    }

    private void fetchChapters(final Course course, final ChapterPager chapterPager) {
        currentPager = chapterPager;
        chapterPager.enqueueNext(new TestpressCallback<List<Chapter>>() {
            @Override
            public void onSuccess(List<Chapter> chapters) {
                if (progressDialog.isIndeterminate()) {
                    progressDialog.setIndeterminate(false);
                }
                progressDialog.incrementProgressBy(
                        getIncrementBy(chapterPager, course.getChaptersCount()));

                if (chapterPager.hasMore() && progressDialog.isShowing()) {
                    fetchChapters(course, chapterPager);
                } else {
                    ChapterDao chapterDao = TestpressSDKDatabase.getChapterDao(mActivity);
                    chapterDao.insertOrReplaceInTx(chapters);
                    fetchContents(course, new ContentPager(course.getId(), apiClient));
                }
            }

            @Override
            public void onException(TestpressException exception) {
                handleError(course, exception);
            }
        });
    }

    private void fetchContents(final Course course, final ContentPager contentPager) {
        currentPager = contentPager;
        contentPager.enqueueNext(new TestpressCallback<List<Content>>() {
            @Override
            public void onSuccess(List<Content> contents) {
                if (progressDialog.isIndeterminate()) {
                    progressDialog.setIndeterminate(false);
                }
                progressDialog.incrementProgressBy(
                        getIncrementBy(contentPager, course.getContentsCount()));

                if (contentPager.hasMore() && progressDialog.isShowing()) {
                    fetchContents(course, contentPager);
                } else {
                    progressDialog.setProgress(progressDialog.getMax());
                    ContentDao contentDao = TestpressSDKDatabase.getContentDao(mActivity);
                    contentDao.insertOrReplaceInTx(contents);
                    course.setChildItemsLoaded(true);
                    mCourseDao.insertOrReplace(course);
                    progressDialog.dismiss();
                    displayChapters(course);
                }
            }

            @Override
            public void onException(TestpressException exception) {
                handleError(course, exception);
            }
        });
    }

    private CourseCredit getCourseCreditByCourseId(long courseId){
        CourseCreditDao courseCreditDao = TestpressSDKDatabase.getCoursesCreditDao(mActivity);
        List<CourseCredit> courseCreditList = courseCreditDao.queryBuilder()
                .where(CourseCreditDao.Properties.Course.eq(courseId)).list();

        if (courseCreditList.size() > 0) {
            return courseCreditList.get(0);
        }

        return null;
    }

    public int calculateCourseProgressPercentage(CourseCredit courseCredit, int totalContents) {

        if (courseCredit != null && totalContents > 0) {
            int total_unique_attempts = courseCredit.getTotalUniqueVideoAttempts()
                    + courseCredit.getTotalUniqueHtmlAttempts()
                    + courseCredit.getTotalUniqueQuizAttempts()
                    + courseCredit.getTotalUniqueExamAttempts()
                    + courseCredit.getTotalUniqueAttachmentAttempts();

            return (total_unique_attempts * 100) / totalContents;
        } else {
            return 0;
        }
    }

    private void restartLoading(Course course) {
        if (currentPager instanceof ChapterPager) {
            fetchChapters(course, (ChapterPager) currentPager);
        } else {
            fetchContents(course, (ContentPager) currentPager);
        }
    }

    private void handleError(final Course course, TestpressException exception) {
        int title;
        int message;
        if (exception.isCancelled()) {
            return;
        } else if (exception.isNetworkError()) {
            title = R.string.testpress_network_error;
            message = R.string.testpress_no_internet_try_again;
        } else {
            title = R.string.testpress_loading_failed;
            message = R.string.testpress_some_thing_went_wrong_try_again;
        }
        retryDialog = new AlertDialog.Builder(mActivity, R.style.TestpressAppCompatAlertDialogStyle)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.testpress_retry_again,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                restartLoading(course);
                            }
                        })
                .setNegativeButton(R.string.testpress_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressDialog.dismiss();
                        wantToCancelDialog.show();
                    }
                })
                .show();
    }

    private int getIncrementBy(BaseResourcePager pager, int totalActiveItems) {
        int totalItemsCount = pager.getTotalItemsCount();
        int totalPages = (totalItemsCount / 200) + (((totalItemsCount % 200) != 0) ? 1 : 0);
        return totalActiveItems / totalPages;
    }

    private void displayChapters(Course course) {
        if (wantToCancelDialog != null && wantToCancelDialog.isShowing()) {
            wantToCancelDialog.dismiss();
        }
        mActivity.startActivity(ExpandableContentsActivity.createIntent(
                course.getTitle(),
                course.getId(),
                mActivity
        ));
    }

    void cancelLoadersIfAny() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (wantToCancelDialog != null && wantToCancelDialog.isShowing()) {
            wantToCancelDialog.dismiss();
        }
        if (retryDialog != null && retryDialog.isShowing()) {
            retryDialog.dismiss();
        }
        if (currentPager != null) {
            currentPager.cancelAsyncRequest();
        }
    }

}
