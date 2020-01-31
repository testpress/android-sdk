package in.testpress.course.ui.fragments.content_detail_fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import junit.framework.Assert;

import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.course.ui.ContentActivity;
import in.testpress.course.ui.fragments.BookmarkFragment;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.network.RetrofitCall;
import in.testpress.util.ViewUtils;

import static in.testpress.course.TestpressCourse.PRODUCT_SLUG;
import static in.testpress.course.network.TestpressCourseApiClient.CONTENTS_PATH_v2_4;
import static in.testpress.course.ui.ContentActivity.ACTIONBAR_TITLE;
import static in.testpress.course.ui.ContentActivity.CHAPTER_ID;
import static in.testpress.course.ui.ContentActivity.CONTENT_ID;
import static in.testpress.course.ui.ContentActivity.GO_TO_MENU;
import static in.testpress.course.ui.ContentActivity.POSITION;
import static in.testpress.course.ui.ContentActivity.TESTPRESS_CONTENT_SHARED_PREFS;

abstract public class BaseContentDetailFragment extends Fragment implements BookmarkFragment.BookmarkListener {
    protected SwipeRefreshLayout swipeRefresh;
    protected Content content;
    private String contentId;
    protected LinearLayout emptyContainer;
    protected TextView emptyTitleView;
    protected TextView emptyDescView;
    protected Button retryButton;
    protected Toast toast;
    protected TestpressCourseApiClient courseApiClient;
    private RetrofitCall<Content> updateContentApiRequest;
    protected ContentDao contentDao;
    protected Button previousButton;
    protected Button nextButton;
    protected TextView pageNumber;
    private RelativeLayout contentView;
    private TestpressExamApiClient examApiClient;
    private LinearLayout buttonLayout;

    private String productSlug;

    private Long chapterId;
    private List<Content> contents;
    private int position;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentDao = TestpressSDKDatabase.getContentDao(getActivity());
        examApiClient = new TestpressExamApiClient(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {        Log.d("BaseContentDetail", "onViewCreated: ");
        return inflater.inflate(R.layout.testpress_activity_content_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        contentView = (RelativeLayout) view.findViewById(R.id.main_content);
        emptyContainer = (LinearLayout) view.findViewById(R.id.empty_container);
        emptyTitleView = (TextView) view.findViewById(R.id.empty_title);
        emptyDescView = (TextView) view.findViewById(R.id.empty_description);
        retryButton = (Button) view.findViewById(R.id.retry_button);
        toast = Toast.makeText(getActivity(), R.string.testpress_no_internet_try_again, Toast.LENGTH_SHORT);
        courseApiClient = new TestpressCourseApiClient(getActivity());
        previousButton = (Button) view.findViewById(R.id.previous);
        nextButton = (Button) view.findViewById(R.id.next);
        pageNumber = (TextView) view.findViewById(R.id.page_number);
        buttonLayout = (LinearLayout) view.findViewById(R.id.button_layout);

        ViewUtils.setTypeface(
                new TextView[] {previousButton, nextButton, pageNumber},
                TestpressSdk.getRubikMediumFont(getActivity())
        );

        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefresh.setColorSchemeResources(R.color.testpress_color_primary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateContent();
            }
        });

        chapterId = getArguments().getLong(CHAPTER_ID, 0);
        if (chapterId != 0) {
            contents = getContentsFromDB();
        }
        if (contents == null || contents.isEmpty()) {
            contentId = getArguments().getString(CONTENT_ID);
            if (contentId == null) {
                Assert.assertNotNull("contentId must not be null.", contents);
            } else {
                updateContent();
            }
        } else {
            position = getArguments().getInt(POSITION, -1);
            if (position == -1) {
                throw new IllegalArgumentException("POSITION must not be null.");
            }
            content = contents.get(position);
            pageNumber.setText(String.format("%d/%d", position + 1, contents.size()));
            String title = getArguments().getString(ACTIONBAR_TITLE);
            Assert.assertNotNull("ACTIONBAR_TITLE must not be null.", title);
            ((ContentActivity)getActivity()).getSupportActionBar().setTitle(title);
        }
        productSlug = getArguments().getString(PRODUCT_SLUG);
        validateAdjacentNavigationButton();
        BookmarkFragment bookmarkFragment = new BookmarkFragment();
        bookmarkFragment.setBookmarkListener(this);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.bookmark_fragment_layout, bookmarkFragment);
        transaction.commit();
    }

    private void validateAdjacentNavigationButton() {
        if (contents == null || productSlug != null) {
            // Discard navigation buttons if deep linked
            return;
        }
        // Set previous button
        if (position == 0) {
            previousButton.setVisibility(View.INVISIBLE);
        } else {
            final int previousPosition = position - 1;
            previousButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().startActivity(ContentActivity.createIntent(previousPosition, chapterId,
                            (ContentActivity)getActivity(), productSlug));
                    getActivity().finish();

                }
            });
            previousButton.setVisibility(View.VISIBLE);
        }
        // Set next button
        if (position == (contents.size() - 1)) {
            nextButton.setText(R.string.testpress_menu);
            nextButton.setVisibility(View.VISIBLE);
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences prefs = getActivity().getSharedPreferences(
                            TESTPRESS_CONTENT_SHARED_PREFS, Context.MODE_PRIVATE);
                    prefs.edit().putBoolean(GO_TO_MENU, true).apply();
                }
            });
        } else {
            final int nextPosition = position + 1;
            if (contents.get(nextPosition).getIsLocked()) {
                nextButton.setVisibility(View.INVISIBLE);
            } else {
                nextButton.setText(R.string.testpress_next_content);
                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().startActivity(ContentActivity.createIntent(nextPosition, chapterId,
                                (ContentActivity)getActivity(), productSlug));
                        getActivity().finish();

                    }
                });
                nextButton.setVisibility(View.VISIBLE);
            }
        }
        buttonLayout.setVisibility(View.VISIBLE);
    }


    List<Content> getContentsFromDB() {
        return contentDao.queryBuilder()
                .where(
                        ContentDao.Properties.ChapterId.eq(chapterId),
                        ContentDao.Properties.Active.eq(true)
                )
                .orderAsc(ContentDao.Properties.Order).list();
    }


    private void showLoadingProgress() {
        if (!swipeRefresh.isRefreshing()) {
            swipeRefresh.setVisibility(View.VISIBLE);
            swipeRefresh.setRefreshing(true);
        }
    }


    public void updateContent() {
        showLoadingProgress();
        hideContents();
        String contentUrl;
        if (content != null) {
            contentUrl = CONTENTS_PATH_v2_4 + content.getId();
        } else {
            contentUrl = CONTENTS_PATH_v2_4 + contentId;
        }

        updateContentApiRequest = courseApiClient.getContent(contentUrl)
            .enqueue(new TestpressCallback<Content>() {
                @Override
                public void onSuccess(Content fetchedContent) {
                   onUpdateContent(fetchedContent);
                }

                @Override
                public void onException(TestpressException exception) {
                    handleError(exception, true);
                }
            });
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        emptyContainer.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
        swipeRefresh.setRefreshing(false);
        swipeRefresh.setVisibility(View.GONE);
        retryButton.setVisibility(View.VISIBLE);
    }

    private void handleError(TestpressException exception, final boolean onUpdateContent) {
        if (exception.isForbidden()) {
            setEmptyText(R.string.permission_denied,
                    R.string.testpress_no_permission,
                    R.drawable.ic_error_outline_black_18dp);

            retryButton.setVisibility(View.GONE);
        } else if (exception.isNetworkError()) {
            if (!swipeRefresh.isRefreshing()) {
                if(!toast.getView().isShown()) {
                    toast.show();
                }
                return;
            }
            setEmptyText(R.string.testpress_network_error,
                    R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);

            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emptyContainer.setVisibility(View.GONE);
                    if (onUpdateContent) {
                        updateContent();
                    } else {
                        loadContent();
                    }
                }
            });
        }  else if (exception.getResponse().code() == 404) {
            setEmptyText(R.string.testpress_content_not_available,
                    R.string.testpress_content_not_available_description,
                    R.drawable.ic_error_outline_black_18dp);

            retryButton.setVisibility(View.GONE);
        } else {
            setEmptyText(R.string.testpress_error_loading_contents,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);

            retryButton.setVisibility(View.GONE);
        }
    }

    public void createContentAttempt() {
        courseApiClient.createContentAttempt(content.getId())
            .enqueue(new TestpressCallback<CourseAttempt>() {
                @Override
                public void onSuccess(CourseAttempt courseAttempt) {
                    onCreateContentAttempt();
                }

                @Override
                public void onException(TestpressException exception) {
                    if (content.getRawVideo() != null && content.isNonEmbeddableVideo()) {
                        handleError(exception, false);
                    } else if (!exception.isNetworkError()) {
                        exception.printStackTrace();
                    }
                }
            });
    }

    abstract void loadContent();

    abstract void onUpdateContent(Content content);

    abstract void onCreateContentAttempt();

    abstract void hideContents();

    @Override
    public Long getBookmarkId() {
        return content.getBookmarkId();
    }

    @Override
    public Long getContentId() {
        return content.getId();
    }

    @Override
    public void onBookmarkSuccess(Long bookmarkId) {
        content.setBookmarkId(bookmarkId);
        contentDao.updateInTx(content);
    }

    @Override
    public void onDeleteBookmarkSuccess() {
        content.setBookmarkId(null);
        contentDao.updateInTx(content);
    }

}
