package in.testpress.course.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
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
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.network.BaseDatabaseModelPager;
import in.testpress.network.BaseResourcePager;
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
    private List<Course> courses;
    private String product_slug;

    CourseListAdapter(Activity activity, CourseDao courseDao, List<Course> courses, String product_slug) {
        super(activity.getLayoutInflater(), R.layout.testpress_course_list_item);
        mActivity = activity;
        mImageLoader = ImageUtils.initImageLoader(activity);
        mOptions = ImageUtils.getPlaceholdersOption();
        mCourseDao = courseDao;
        apiClient = new TestpressCourseApiClient(activity);
        initProgressDialog(activity);
        wantToCancelDialog = new AlertDialog.Builder(mActivity, R.style.TestpressAppCompatAlertDialogStyle)
                .setTitle(R.string.testpress_are_you_sure)
                .setMessage("Do you want to cancel")
                .setPositiveButton(R.string.testpress_yes, null)
                .create();
        this.courses = courses;
        this.product_slug = product_slug;
    }

    private void initProgressDialog(Context context) {
        progressDialog = new ProgressDialog(context, R.style.TestpressAppCompatAlertDialogStyle);
        progressDialog.setMessage("Fetching contents");
        progressDialog.setProgressStyle(STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }

    @Override
    public int getCount() {
        if (!courses.isEmpty()) {
            return courses.size();
        }
        return (int) mCourseDao.queryBuilder().where(CourseDao.Properties.IsProduct.isNull()).count();
    }

    @Override
    public Course getItem(int position) {
        if (!courses.isEmpty()) {
            return courses.get(position);
        }
        return mCourseDao.queryBuilder()
                .where(CourseDao.Properties.IsProduct.isNull())
                .orderAsc(CourseDao.Properties.Order)
                .listLazy().get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.course_title, R.id.thumbnail_image, R.id.percentage,
                R.id.course_item_layout,  R.id.progress_bar_layout, R.id.external_link_title};
    }

    @Override
    protected void update(final int position, final Course course) {

        setFont(new int[]{0, 2}, TestpressSdk.getRubikMediumFont(mActivity));
        setText(0, course.getTitle());
        if (course.getImage() == null || course.getImage().isEmpty()) {
            setGone(1, true);
        } else {
            setGone(1, false);
            mImageLoader.displayImage(course.getImage(), imageView(1), mOptions);
        }

        setTextToTextView(course.getExternal_link_label(), (TextView) view(5));
        toggleTextViewVisibility(!course.isCourseForRegistration(), view(5));

        view(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("CourseListAdapter", "onClick: " + course.getChildItemsLoaded() + course.getChaptersCount());
                if (course.getChildItemsLoaded() || course.getChaptersCount() == 0) {
                    openCourseContentsOrExternalLink(mActivity, course, !course.isCourseForRegistration());
                } else {
                    showProgressDialog(course);
                }
            }
        });
        // ToDo: Set completed percentage in the progress bar
        setGone(4, true);
    }

    public void toggleTextViewVisibility(boolean toHide, View view) {
        if (toHide) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    public void setTextToTextView(String textViewText, TextView textView) {
        if (!textViewText.equals("")) {
            textView.setText(textViewText);
        }
    }

    public void openCourseContentsOrExternalLink(Activity activity, Course course, boolean openCourseContent) {

        if (wantToCancelDialog != null && wantToCancelDialog.isShowing()) {
            wantToCancelDialog.dismiss();
        }
        if (openCourseContent) {
            activity.startActivity(ChapterDetailActivity.createIntent(
                    course.getTitle(),
                    course.getId(),
                    activity, this.product_slug));
        } else {
            Intent intent = new Intent(activity, WebViewActivity.class);
            intent.putExtra("URL", course.getExternal_content_link());
            intent.putExtra("TITLE", course.getExternal_link_label());
            activity.startActivity(intent);
        }
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
        fetchChapters(course, new ChapterPager(course.getId().toString(), apiClient));
    }

    private void increaseProgress(BaseResourcePager pager, int count) {
        if (progressDialog.isIndeterminate()) {
            progressDialog.setIndeterminate(false);
        }
        progressDialog.incrementProgressBy(
                getIncrementBy(pager, count));

    }

    private void fetchChapters(final Course course, final ChapterPager chapterPager) {
        currentPager = chapterPager;
        chapterPager.enqueueNext(new TestpressCallback<List<Chapter>>() {
            @Override
            public void onSuccess(List<Chapter> chapters) {
                increaseProgress(chapterPager, course.getChaptersCount());

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
                increaseProgress(contentPager, course.getContentsCount());

                if (contentPager.hasMore() && progressDialog.isShowing()) {
                    fetchContents(course, contentPager);
                } else {
                    progressDialog.setProgress(progressDialog.getMax());
                    ContentDao contentDao = TestpressSDKDatabase.getContentDao(mActivity);
                    contentDao.insertOrReplaceInTx(contents);
                    Log.d("CourseListAdapter", "onSuccess: " + course.getTitle());
                    course.setChildItemsLoaded(true);
                    mCourseDao.insertOrReplace(course);
                    progressDialog.dismiss();
                    openCourseContentsOrExternalLink(mActivity, course, !course.isCourseForRegistration());
                }
            }

            @Override
            public void onException(TestpressException exception) {
                handleError(course, exception);
            }
        });
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

}
