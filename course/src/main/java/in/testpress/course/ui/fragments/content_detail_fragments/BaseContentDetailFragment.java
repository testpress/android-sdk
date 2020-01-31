package in.testpress.course.ui.fragments.content_detail_fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import junit.framework.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.course.ui.ContentActivity;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.ui.FolderSpinnerAdapter;
import in.testpress.models.greendao.Attachment;
import in.testpress.models.greendao.Bookmark;
import in.testpress.models.greendao.BookmarkFolder;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.Video;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.view.ClosableSpinner;
import in.testpress.util.ViewUtils;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.FolderListResponse;

import static in.testpress.course.TestpressCourse.PRODUCT_SLUG;
import static in.testpress.course.network.TestpressCourseApiClient.CONTENTS_PATH_v2_4;
import static in.testpress.course.ui.ContentActivity.ACTIONBAR_TITLE;
import static in.testpress.course.ui.ContentActivity.CHAPTER_ID;
import static in.testpress.course.ui.ContentActivity.CONTENT_ID;
import static in.testpress.course.ui.ContentActivity.GO_TO_MENU;
import static in.testpress.course.ui.ContentActivity.POSITION;
import static in.testpress.course.ui.ContentActivity.TESTPRESS_CONTENT_SHARED_PREFS;
import static in.testpress.exam.network.TestpressExamApiClient.BOOKMARK_FOLDERS_PATH;
import static in.testpress.models.greendao.BookmarkFolder.UNCATEGORIZED;

abstract public class BaseContentDetailFragment extends Fragment {
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


    private LottieAnimationView animationView;
    private TextView bookmarkButtonText;
    private ImageView bookmarkButtonImage;
    private RelativeLayout bookmarkLayout;
    private LinearLayout bookmarkButtonLayout;
    private ArrayList<BookmarkFolder> bookmarkFolders = new ArrayList<>();
    private ClosableSpinner bookmarkFolderSpinner;
    private FolderSpinnerAdapter folderSpinnerAdapter;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
        initBookmark();
        productSlug = getArguments().getString(PRODUCT_SLUG);
        validateAdjacentNavigationButton();
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


    private void initBookmark() {
        View view = getView();
        animationView = view.findViewById(R.id.bookmark_loader);
        animationView.playAnimation();
        bookmarkLayout = view.findViewById(R.id.bookmark_layout);
        bookmarkButtonLayout = view.findViewById(R.id.bookmark_button_layout);
        bookmarkButtonImage = view.findViewById(R.id.bookmark_button_image);
        bookmarkButtonText = view.findViewById(R.id.bookmark_text);
        bookmarkButtonText.setTypeface(TestpressSdk.getRubikRegularFont(getActivity()));
        bookmarkButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (content.getBookmarkId() != null) {
                    deleteBookmark(content.getBookmarkId());
                } else {
                    String baseUrl = TestpressSdk.getTestpressSession(getActivity())
                            .getInstituteSettings().getBaseUrl();

                    bookmarkFolders.clear();
                    loadBookmarkFolders(baseUrl + BOOKMARK_FOLDERS_PATH);
                }
            }
        });
        bookmarkFolderSpinner = view.findViewById(R.id.bookmark_folder_spinner);
        folderSpinnerAdapter = new FolderSpinnerAdapter(getActivity(), getResources(),
                new ViewUtils.OnInputCompletedListener() {
                    @Override
                    public void onInputComplete(String folderName) {
                        bookmarkFolderSpinner.dismissPopUp();
                        bookmark(folderName);
                    }
                });
        folderSpinnerAdapter.hideSpinner(true);
        bookmarkFolderSpinner.setAdapter(folderSpinnerAdapter);
        bookmarkFolderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                if (position == 0) {
                    return;
                }
                bookmark(folderSpinnerAdapter.getTag(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    void bookmark(String folder) {
        setBookmarkProgress(true);
        examApiClient.bookmark(content.getId(), folder, "chaptercontent", "courses")
                .enqueue(new TestpressCallback<Bookmark>() {
                    @Override
                    public void onSuccess(Bookmark bookmark) {
                        content.setBookmarkId(bookmark.getId());
                        contentDao.updateInTx(content);
                        bookmarkButtonText.setText(R.string.testpress_remove_bookmark);
                        bookmarkButtonImage.setImageResource(R.drawable.ic_remove_bookmark);
                        setBookmarkProgress(false);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        setBookmarkProgress(false);
                        handleException(exception);
                    }
                });
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

    abstract void hideContents();

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

    void setBookmarkProgress(boolean show) {
        if (show) {
            bookmarkButtonLayout.setVisibility(View.GONE);
            animationView.setVisibility(View.VISIBLE);
        } else {
            animationView.setVisibility(View.GONE);
            bookmarkButtonLayout.setVisibility(View.VISIBLE);
        }
    }

    void deleteBookmark(Long bookmarkId) {
        setBookmarkProgress(true);
        examApiClient.deleteBookmark(bookmarkId)
                .enqueue(new TestpressCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        content.setBookmarkId(null);
                        contentDao.updateInTx(content);
                        bookmarkFolderSpinner.setSelection(0);
                        bookmarkButtonText.setText(R.string.testpress_bookmark_this);
                        bookmarkButtonImage.setImageResource(R.drawable.ic_bookmark);
                        setBookmarkProgress(false);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        setBookmarkProgress(false);
                        handleException(exception);
                    }
                });
    }

    void loadBookmarkFolders(String url) {
        setBookmarkProgress(true);
        examApiClient.getBookmarkFolders(url)
                .enqueue(new TestpressCallback<ApiResponse<FolderListResponse>>() {
                    @Override
                    public void onSuccess(ApiResponse<FolderListResponse> apiResponse) {
                        bookmarkFolders.addAll(apiResponse.getResults().getFolders());
                        if (apiResponse.getNext() != null) {
                            loadBookmarkFolders(apiResponse.getNext());
                        } else {
                            addFoldersToSpinner(bookmarkFolders);
                            setBookmarkProgress(false);
                            bookmarkFolderSpinner.performClick();
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        setBookmarkProgress(false);
                        handleException(exception);
                    }
                });
    }

    void addFoldersToSpinner(List<BookmarkFolder> bookmarkFolders) {
        folderSpinnerAdapter.clear();
        folderSpinnerAdapter.addHeader("-- Select Folder --");
        for (BookmarkFolder folder: bookmarkFolders) {
            folderSpinnerAdapter.addItem(folder.getName(), folder.getName(), false, 0);
        }
        folderSpinnerAdapter.addItem(null, UNCATEGORIZED, false, 0);
        folderSpinnerAdapter.notifyDataSetChanged();
    }


    abstract void loadContent();

    abstract void onUpdateContent(Content content);

    abstract void onCreateContentAttempt();

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

    void handleException(TestpressException exception) {
        if(exception.isUnauthenticated()) {
            Snackbar.make(contentView, R.string.testpress_authentication_failed,
                    Snackbar.LENGTH_SHORT).show();
        } else if (exception.getCause() instanceof IOException) {
            Snackbar.make(contentView, R.string.testpress_no_internet_connection,
                    Snackbar.LENGTH_SHORT).show();
        } else if (exception.isClientError()) {
            Snackbar.make(contentView, R.string.testpress_folder_name_not_allowed,
                    Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(contentView, R.string.testpress_network_error,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

}
