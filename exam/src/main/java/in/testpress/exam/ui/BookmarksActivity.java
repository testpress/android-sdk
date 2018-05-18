package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.network.BookmarksPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.models.greendao.Bookmark;
import in.testpress.models.greendao.BookmarkDao;
import in.testpress.models.greendao.BookmarkFolder;
import in.testpress.models.greendao.BookmarkFolderDao;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ReviewItem;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.ui.HeaderFooterListAdapter;
import in.testpress.ui.view.ClosableSpinner;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ThrowableLoader;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.FolderListResponse;

import static in.testpress.exam.network.TestpressExamApiClient.BOOKMARK_FOLDERS_PATH;
import static in.testpress.models.greendao.BookmarkFolder.UNCATEGORIZED;
import static in.testpress.network.TestpressApiClient.MODIFIED;
import static in.testpress.network.TestpressApiClient.SINCE;
import static in.testpress.network.TestpressApiClient.TIME_FIELD;
import static in.testpress.network.TestpressApiClient.UNFILTERED;
import static in.testpress.network.TestpressApiClient.UNTIL;

public class BookmarksActivity extends BaseToolBarActivity
        implements LoaderManager.LoaderCallbacks<List<Bookmark>>, AbsListView.OnScrollListener {

    // Loader for refresh
    private static final int REFRESH_LOADER_ID = 0;

    // Loader to load bottom old bookmarks
    private static final int LOADER_ID = 1;

    // Number of maximum bookmarks which can be missed from the latest
    private static final int MISSED_BOOKMARKS_THRESHOLD = 50;

    private ViewPager viewPager;
    private BookmarkPagerAdapter pagerAdapter;
    private HeaderFooterListAdapter listAdapter;
    private SwipeRefreshLayout listViewSwipeRefreshLayout;
    private SwipeRefreshLayout viewPagerSwipeRefreshLayout;
    private View contentLayout;
    private View buttonLayout;
    private ListView listView;
    private View emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private ImageView emptyImageView;
    private Button retryButton;
    private Button previousButton;
    private Button nextButton;
    private ClosableSpinner folderSpinner;
    private LinearLayout newBookmarksAvailableLabel;
    private BookmarksPager refreshPager;
    private BookmarksPager pager;
    private EditableItemSpinnerAdapter foldersSpinnerAdapter;
    private View loadingLayout;
    private int lastFirstVisibleItem;
    private int lastFirstVisibleItemTop;
    private boolean isUserSwiped;
    private boolean isLoadingNewBookmarks;
    private TestpressExamApiClient apiClient;
    private String currentFolder = "";
    private BookmarkFolderDao folderDao;
    private BookmarkDao bookmarkDao;
    private List<BookmarkFolder> bookmarkFolders = new ArrayList<>();
    private ProgressDialog progressDialog;

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_bookmarks);
        listView = findViewById(R.id.list_view);
        emptyView = findViewById(R.id.empty_container);
        emptyTitleView = findViewById(R.id.empty_title);
        emptyDescView = findViewById(R.id.empty_description);
        emptyImageView = findViewById(R.id.image_view);
        retryButton = findViewById(R.id.retry_button);
        previousButton = findViewById(R.id.previous);
        nextButton = findViewById(R.id.next);
        contentLayout = findViewById(R.id.content_layout);
        buttonLayout = findViewById(R.id.button_layout);
        viewPager = findViewById(R.id.pager);
        newBookmarksAvailableLabel = findViewById(R.id.new_bookmarks_available_label);
        ProgressBar progressBar = findViewById(R.id.pb_loading);
        progressBar.setVisibility(View.GONE);
        loadingLayout = getLayoutInflater().inflate(R.layout.testpress_loading_layout, null);
        ProgressBar footerProgressBar = loadingLayout.findViewById(R.id.progress_bar);
        UIUtils.setIndeterminateDrawable(this, footerProgressBar, 3);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.testpress_please_wait));
        progressDialog.setCancelable(false);
        UIUtils.setIndeterminateDrawable(this, progressDialog, 4);

        apiClient = new TestpressExamApiClient(this);
        bookmarkDao = TestpressSDKDatabase.getBookmarkDao(this);

        ViewUtils.setTypeface(
                new TextView[] { nextButton, previousButton, emptyTitleView },
                TestpressSdk.getRubikMediumFont(this)
        );
        emptyDescView.setTypeface(TestpressSdk.getRubikRegularFont(this));
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPrevious();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNext();
            }
        });
        newBookmarksAvailableLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayNewBookmarks();
            }
        });
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emptyView.setVisibility(View.GONE);
                if (buttonLayout.getVisibility() == View.VISIBLE) {
                    contentLayout.setVisibility(View.VISIBLE);
                    viewPagerSwipeRefreshLayout.setRefreshing(true);
                    viewPager.setVisibility(View.GONE);
                    loadMoreBookmarks();
                } else {
                    refreshWithProgress();
                }
            }
        });
        listViewSwipeRefreshLayout = findViewById(R.id.list_view_swipe_container);
        listViewSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWithProgress();
            }
        });
        listViewSwipeRefreshLayout.setColorSchemeResources(R.color.testpress_color_primary);
        viewPagerSwipeRefreshLayout = findViewById(R.id.pager_swipe_container);
        viewPagerSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWithProgress();
            }
        });
        viewPagerSwipeRefreshLayout.setColorSchemeResources(R.color.testpress_color_primary);
        listAdapter = new HeaderFooterListAdapter<SingleTypeAdapter<Bookmark>>(
                listView,
                new BookmarksListAdapter(this, currentFolder)
        );
        listView.setAdapter(listAdapter);
        listView.setOnScrollListener(this);
        listView.setFastScrollEnabled(true);
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position,
                                            long id) {

                        if (viewPager.getCurrentItem() != position) {
                            viewPager.setCurrentItem(position, false);
                        } else {
                            updateNavigationButtons(position);
                        }
                        showBookmarksList(false);
                    }
                });
        pagerAdapter = new BookmarkPagerAdapter(this, currentFolder);
        viewPager.setAdapter(pagerAdapter);
        goToPosition(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                goToPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        View spinnerContainer =
                getLayoutInflater().inflate(R.layout.testpress_actionbar_spinner, toolbar, false);

        foldersSpinnerAdapter = new EditableItemSpinnerAdapter(this, true,
                new EditableItemSpinnerAdapter.OnEditItemListener() {
                    @Override
                    public void onClickEdit(int position) {
                        showFolderUpdateDialogBox(position);
                    }
        });
        folderSpinner = spinnerContainer.findViewById(R.id.actionbar_spinner);
        folderSpinner.setAdapter(foldersSpinnerAdapter);
        folderDao = TestpressSDKDatabase.getBookmarkFolderDao(this);
        View view = toolbar.findViewById(R.id.actionbar_spinnerwrap);
        toolbar.removeView(view);
        toolbar.invalidate();
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        toolbar.addView(spinnerContainer, lp);
        addFoldersToSpinner();
        bookmarkFolders.clear();
        //noinspection ConstantConditions
        String baseUrl = TestpressSdk.getTestpressSession(this).getInstituteSettings().getBaseUrl();
        loadBookmarkFolders(baseUrl + BOOKMARK_FOLDERS_PATH);
        folderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentFolder = foldersSpinnerAdapter.getTag(i);
                pagerAdapter.setCurrentFolder(currentFolder);
                getBookmarksListAdapter().setCurrentFolder(currentFolder);
                updateItems(true);
                if (viewPager.getCurrentItem() == 0) {
                    updateNavigationButtons(0);
                } else {
                    viewPager.setCurrentItem(0, false);
                }
                lastFirstVisibleItem = 0;
                getBookmarksListAdapter().setBackgroundShadePosition(-1);
                Loader<Bookmark> refreshLoader =
                        getSupportLoaderManager().getLoader(REFRESH_LOADER_ID);

                if (refreshLoader != null && !refreshLoader.isReset()) {
                    getSupportLoaderManager().destroyLoader(REFRESH_LOADER_ID);
                }
                Loader<Bookmark> loader = getSupportLoaderManager().getLoader(LOADER_ID);
                if (loader != null && !loader.isReset()) {
                    getSupportLoaderManager().destroyLoader(LOADER_ID);
                }
                refreshPager = null;
                pager = null;
                getSupportLoaderManager()
                        .restartLoader(REFRESH_LOADER_ID, null, BookmarksActivity.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private QueryBuilder<Bookmark> getQueryBuilder() {
        return Bookmark.getQueryBuilder(this, currentFolder);
    }

    private QueryBuilder<Bookmark> getQueryBuilderToDisplay() {
        return Bookmark.getQueryBuilderToDisplay(this, currentFolder);
    }

    private BookmarksPager getRefreshPager() {
        if (refreshPager == null) {
            refreshPager = new BookmarksPager(this, apiClient, currentFolder);
            QueryBuilder<Bookmark> queryBuilder = getQueryBuilder();
            if (queryBuilder.count() > 0) {
                queryBuilder.orderDesc(BookmarkDao.Properties.ModifiedDate);
                Bookmark latest = queryBuilder.list().get(0);
                refreshPager.setQueryParams(SINCE, latest.getModified());
                refreshPager.setQueryParams(UNFILTERED, true);
                refreshPager.setQueryParams(TIME_FIELD, MODIFIED);
            }
        }
        return refreshPager;
    }

    private BookmarksPager getOldBookmarksLoadingPager() {
        if (pager == null) {
            pager = new BookmarksPager(this, apiClient, currentFolder);
            QueryBuilder<Bookmark> queryBuilder = getQueryBuilder();
            if (queryBuilder.count() > 0) {
                queryBuilder.orderDesc(BookmarkDao.Properties.CreatedDate);
                Bookmark lastBookmark = queryBuilder.list().get((int) queryBuilder.count() - 1);
                pager.setQueryParams(UNTIL, lastBookmark.getCreated());
            }
        }
        return pager;
    }

    void loadBookmarkFolders(String url) {
        new TestpressExamApiClient(this).getBookmarkFolders(url)
                .enqueue(new TestpressCallback<ApiResponse<FolderListResponse>>() {
                    @Override
                    public void onSuccess(ApiResponse<FolderListResponse> apiResponse) {
                        bookmarkFolders.addAll(apiResponse.getResults().getFolders());
                        if (apiResponse.getNext() != null) {
                            loadBookmarkFolders(apiResponse.getNext());
                        } else {
                            folderDao.deleteAll();
                            folderDao.insertOrReplaceInTx(bookmarkFolders);
                            addFoldersToSpinner();
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleException(exception);
                    }
                });
    }

    synchronized void addFoldersToSpinner() {
        foldersSpinnerAdapter.clear();
        foldersSpinnerAdapter.addItem("", "All Bookmarks", false, 0);
        List<BookmarkFolder> bookmarkFolders = folderDao.queryBuilder().list();
        for (BookmarkFolder folder: bookmarkFolders) {
            foldersSpinnerAdapter.addItem(folder.getName(), folder.getName(), false, 0);
        }
        foldersSpinnerAdapter.addItem(UNCATEGORIZED, UNCATEGORIZED, false, 0);
        foldersSpinnerAdapter.notifyDataSetChanged();
    }

    void addNewFolderToSpinner(String folderName) {
        String currentFolder = foldersSpinnerAdapter.getTag(folderSpinner.getSelectedItemPosition());
        int index = foldersSpinnerAdapter.getCount() - 1;
        foldersSpinnerAdapter.addItem(index, folderName, folderName, false, 0);
        foldersSpinnerAdapter.notifyDataSetChanged();
        folderSpinner.setSelection(foldersSpinnerAdapter.getItemPosition(currentFolder));
    }

    void updateFolderSpinnerItem(int position, String folderName) {
        foldersSpinnerAdapter.updateItem(position, folderName, folderName, false, 0);
        foldersSpinnerAdapter.notifyDataSetChanged();
    }

    void deleteFolderSpinnerItem(int position) {
        foldersSpinnerAdapter.removeItem(position);
        foldersSpinnerAdapter.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Loader<List<Bookmark>> onCreateLoader(int loaderID, @Nullable Bundle args) {
        switch (loaderID) {
            case REFRESH_LOADER_ID:
                isLoadingNewBookmarks = true;
                getRefreshPager();
                if (getQueryBuilderToDisplay().count() == 0) {
                    listViewSwipeRefreshLayout.setRefreshing(true);
                    viewPagerSwipeRefreshLayout.setRefreshing(true);
                }
                return new BookmarksLoader(this, getRefreshPager());
            default:
                return new BookmarksLoader(this, getOldBookmarksLoadingPager());
        }
    }

    private static class BookmarksLoader extends ThrowableLoader<List<Bookmark>> {

        private BookmarksPager pager;

        BookmarksLoader(Context context, BookmarksPager pager) {
            super(context, null);
            this.pager = pager;
        }

        @Override
        public List<Bookmark> loadData() throws TestpressException {
            pager.next();
            return pager.getListResponse().getBookmarks();
        }
    }

    @Override
    public void onLoadFinished(@NonNull final Loader<List<Bookmark>> loader,
                               List<Bookmark> bookmarks) {

        if (loader.getId() == REFRESH_LOADER_ID) {
            listViewSwipeRefreshLayout.setRefreshing(false);
            viewPagerSwipeRefreshLayout.setRefreshing(false);
            isLoadingNewBookmarks = false;
        }
        getSupportLoaderManager().destroyLoader(loader.getId());
        TestpressException exception =
                ((ThrowableLoader<List<Bookmark>>) loader).clearException();

        if(exception != null) {
            if (listAdapter.getFootersCount() != 0) {
                listAdapter.removeFooter(loadingLayout);
            }
            if (loader.getId() == LOADER_ID &&
                    (viewPager.getCurrentItem() + 1) == pagerAdapter.getCount() &&
                    viewPagerSwipeRefreshLayout.getVisibility() == View.VISIBLE &&
                    viewPager.getVisibility() == View.GONE) {

                if (!isLoadingNewBookmarks) {
                    viewPagerSwipeRefreshLayout.setRefreshing(false);
                }
                setEmptyText(R.string.testpress_network_error,
                        R.string.testpress_no_internet_try_again,
                        R.drawable.testpress_no_wifi,
                        false);

                return;
            }
            if (pagerAdapter.getCount() != 0) {
                handleException(exception);
            } else if(exception.isUnauthenticated()) {
                setEmptyText(R.string.testpress_authentication_failed,
                        R.string.testpress_please_login,
                        R.drawable.testpress_alert_warning,
                        true);
            } else if (exception.isNetworkError()) {
                setEmptyText(R.string.testpress_network_error,
                        R.string.testpress_no_internet_try_again,
                        R.drawable.testpress_no_wifi,
                        true);
            } else {
                setEmptyText(R.string.testpress_error_loading_bookmarks,
                        R.string.testpress_some_thing_went_wrong_try_again,
                        R.drawable.testpress_alert_warning,
                        true);
            }
            return;
        }

        switch (loader.getId()) {
            case REFRESH_LOADER_ID:
                onRefreshLoadFinished();
                break;
            case LOADER_ID:
                onNetworkLoadFinished();
                break;
        }
    }

    void onRefreshLoadFinished() {
        if (refreshPager == null) {
            return;
        }
        // If no data is available in the local database, directly insert & display from database
        if ((getQueryBuilderToDisplay().count() == 0) || refreshPager.getResources().isEmpty()) {
            if (!refreshPager.getResources().isEmpty()) {
                saveItems(refreshPager);
            }
            updateItems(false);
        } else {
            // If data is already available in the local database, then
            // notify user about the new data to view latest data.
            if (getQueryBuilder().count() != 0 &&
                    MISSED_BOOKMARKS_THRESHOLD >= refreshPager.getTotalCount() &&
                    refreshPager.hasNext()) {

                getSupportLoaderManager().restartLoader(REFRESH_LOADER_ID, null, this);
                return;
            }
            if (isUserSwiped || (lastFirstVisibleItem == 0)) {
                displayNewBookmarks();
            } else {
                newBookmarksAvailableLabel.setVisibility(View.VISIBLE);
            }
        }
    }

    void onNetworkLoadFinished() {
        if (pager == null) {
            return;
        }
        if (!pager.hasNext()) {
            if (listAdapter.getFootersCount() != 0) {
                // if pager reached last page, remove footer if footer exists
                listAdapter.removeFooter(loadingLayout);
            }
        }
        if (!pager.getResources().isEmpty()) {
            saveItems(pager);
        }
        int previousItemsCount = pagerAdapter.getCount();
        int currentPosition = viewPager.getCurrentItem();
        updateItems(false);
        if (previousItemsCount == currentPosition + 1 && !nextButton.isEnabled()) {
            viewPager.setCurrentItem(currentPosition + 1, false);
        }
        if (!isLoadingNewBookmarks) {
            viewPagerSwipeRefreshLayout.setRefreshing(false);
        }
        viewPager.setVisibility(View.VISIBLE);
    }

    private void saveItems(BookmarksPager pager) {
        Bookmark.save(this, pager.getResources(), currentFolder.isEmpty());
        TestpressSDKDatabase.getBookmarkFolderDao(this).insertOrReplaceInTx(pager.getFolders());
        TestpressSDKDatabase.getContentTypeDao(this).insertOrReplaceInTx(pager.getContentTypes());

        ReviewItem.save(this, pager.getReviewItems());
        TestpressSDKDatabase.getReviewItemDao(this).insertOrReplaceInTx(pager.getReviewItems());
        TestpressSDKDatabase.getReviewQuestionDao(this).insertOrReplaceInTx(pager.getQuestions());
        TestpressSDKDatabase.getReviewAnswerDao(this).insertOrReplaceInTx(pager.getAnswers());
        TestpressSDKDatabase.getReviewQuestionTranslationDao(this)
                .insertOrReplaceInTx(pager.getTranslations());
        TestpressSDKDatabase.getReviewAnswerTranslationDao(this)
                .insertOrReplaceInTx(pager.getAnswerTranslations());

        TestpressSDKDatabase.getDirectionDao(this).insertOrReplaceInTx(pager.getDirections());
        TestpressSDKDatabase.getDirectionTranslationDao(this)
                .insertOrReplaceInTx(pager.getDirectionTranslations());
        TestpressSDKDatabase.getSubjectDao(this).insertOrReplaceInTx(pager.getSubjects());

        Content.save(this, pager.getContents());
        TestpressSDKDatabase.getHtmlContentDao(this).insertOrReplaceInTx(pager.getHtmlContents());
        TestpressSDKDatabase.getVideoDao(this).insertOrReplaceInTx(pager.getVideos());
        TestpressSDKDatabase.getAttachmentDao(this).insertOrReplaceInTx(pager.getAttachments());
    }

    void updateItems(boolean positionModified) {
        listAdapter.notifyDataSetChanged();
        pagerAdapter.notifyDataSetChanged(positionModified);
        if (pagerAdapter.getCount() == 0) {
            setEmptyText(R.string.testpress_no_bookmarks,
                    R.string.testpress_no_bookmarks_description,
                    R.drawable.testpress_bookmark_flat_icon,
                    true);

            retryButton.setVisibility(View.GONE);
        } else {
            contentLayout.setVisibility(View.VISIBLE);
            setNavigationBarVisible(viewPagerSwipeRefreshLayout.getVisibility() == View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    public void displayNewBookmarks() {
        newBookmarksAvailableLabel.setVisibility(View.GONE);
        if (MISSED_BOOKMARKS_THRESHOLD < refreshPager.getTotalCount()) {
            bookmarkDao.deleteAll();
            pager = null;
        }
        if (!refreshPager.getResources().isEmpty()) {
            saveItems(refreshPager);
        }
        updateItems(true);
        viewPager.setCurrentItem(0, false);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {

        if (getSupportLoaderManager().hasRunningLoaders())
            return;

        boolean isScrollingUp;
        if (listView.getChildAt(0) != null) {
            // Detect scrolling up or down
            int currentFirstVisibleItem = listView.getFirstVisiblePosition();
            int currentFirstVisibleItemTop = Math.abs(listView.getChildAt(0).getTop());
            if (currentFirstVisibleItem > lastFirstVisibleItem) {
                isScrollingUp = false;
            } else if (currentFirstVisibleItem < lastFirstVisibleItem) {
                isScrollingUp = true;
            } else if (currentFirstVisibleItemTop > lastFirstVisibleItemTop) {
                isScrollingUp = false;
            } else if (currentFirstVisibleItemTop < lastFirstVisibleItemTop) {
                isScrollingUp = true;
            } else {
                isScrollingUp = false;
            }
        } else {
            return;
        }
        if (!isScrollingUp &&
                (listView.getLastVisiblePosition() + 3) >= getBookmarksListAdapter().getCount()) {

            if (pager != null && !pager.hasMore()) {
                if (listAdapter.getFootersCount() != 0) {
                    // If pager reached last page, remove footer if footer exists
                    listAdapter.removeFooter(loadingLayout);
                }
                return;
            }
            loadMoreBookmarks();
        }
    }

    void loadMoreBookmarks() {
        if (listAdapter.getFootersCount() == 0) {
            // Display loading footer if not present when loading next page
            listAdapter.addFooter(loadingLayout);
        }
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        lastFirstVisibleItem = listView.getFirstVisiblePosition();
        lastFirstVisibleItemTop = Math.abs(listView.getChildAt(0).getTop());
        // While previously loading the next page any error happens, this ensures to try
        // to load the next page if available, while scrolling
        if (pager != null) {
            pager.setHasMore(pager.hasNext());
        }
        getBookmarksListAdapter().setBackgroundShadePosition(-1);
    }

    private void showBookmarksList(boolean show) {
        if(show) {
            listViewSwipeRefreshLayout.setVisibility(View.VISIBLE);
            viewPagerSwipeRefreshLayout.setVisibility(View.GONE);
            getSupportFragmentManager().getFragments().get(0).setUserVisibleHint(true); // Resume web view
        } else {
            viewPagerSwipeRefreshLayout.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
            if (!isLoadingNewBookmarks) {
                viewPagerSwipeRefreshLayout.setRefreshing(false);
            }
            listViewSwipeRefreshLayout.setVisibility(View.GONE);
        }
        setNavigationBarVisible(!show);
    }

    private void showPrevious() {
        if ((viewPager.getCurrentItem() + 1) == pagerAdapter.getCount() &&
                (viewPager.getVisibility() == View.GONE || emptyView.getVisibility() == View.VISIBLE)) {

            if (!isLoadingNewBookmarks) {
                viewPagerSwipeRefreshLayout.setRefreshing(false);
            }
            viewPager.setVisibility(View.VISIBLE);
            enableButton(nextButton, true);
            enableButton(previousButton, viewPager.getCurrentItem() != 0);
            contentLayout.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    private void showNext() {
        if ((viewPager.getCurrentItem() + 1) == pagerAdapter.getCount()) {
            // Reached last item
            if (listAdapter.getFootersCount() != 0) {
                viewPagerSwipeRefreshLayout.setRefreshing(true);
                viewPager.setVisibility(View.GONE);
            } else {
                setEmptyText(R.string.testpress_network_error,
                        R.string.testpress_no_internet_try_again,
                        R.drawable.testpress_no_wifi,
                        false);
            }
            enableButton(nextButton, false);
            enableButton(previousButton, true);
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        }
    }

    private void goToPosition(int position) {
        if (pagerAdapter.getCount() == 0) {
            return;
        }
        listView.setSelection(viewPager.getCurrentItem());
        listView.smoothScrollToPosition(viewPager.getCurrentItem());

        updateNavigationButtons(position);
    }

    private void updateNavigationButtons(int currentPosition) {
        if (currentPosition == 0) {
            // Reached first item
            enableButton(previousButton, false);
        } else {
            enableButton(previousButton, true);
        }
        if ((currentPosition + 1) == pagerAdapter.getCount()) {
            // Reached last item
            if (pager != null && pager.hasNext()) {
                enableButton(nextButton, true);
            } else {
                enableButton(nextButton, false);
            }
        } else {
            enableButton(nextButton, true);
        }
    }

    /**
     * Set alpha to the navigation buttons.
     *
     * @param button Button, which need to be enable or disable.
     * @param enable True if enable, false otherwise.
     */
    private void enableButton(Button button, boolean enable) {
        if (enable) {
            button.setTextColor(ContextCompat.getColor(this, R.color.testpress_text_gray));
            ViewUtils.setDrawableColor(button, R.color.testpress_text_gray);
        } else {
            button.setTextColor(ContextCompat.getColor(this, R.color.testpress_gray_light));
            ViewUtils.setDrawableColor(button, R.color.testpress_gray_light);
        }
        button.setEnabled(enable);
    }

    @SuppressLint("InflateParams")
    public void showFolderUpdateDialogBox(final int position) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle);

        builder.setTitle(R.string.testpress_rename_folder);

        View dialogView = getLayoutInflater().inflate(R.layout.testpress_edit_text_dialog_box, null);
        builder.setView(dialogView);

        final EditText editText = dialogView.findViewById(R.id.edit_text);
        final String folderName = foldersSpinnerAdapter.getTag(position);
        editText.setText(folderName);
        editText.setSelection(folderName.length());
        final BookmarkFolder folder = folderDao.queryBuilder()
                .where(BookmarkFolderDao.Properties.Name.eq(folderName)).list().get(0);

        builder.setPositiveButton(R.string.testpress_update, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                UIUtils.hideSoftKeyboard(BookmarksActivity.this, editText);
                String inputText = editText.getText().toString();
                if (inputText.trim().isEmpty() || inputText.trim().equals(folderName)) {
                    return;
                }
                updateBookmarkFolder(folder.getId(), inputText, position);
            }
        });
        builder.setNegativeButton(R.string.testpress_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                UIUtils.hideSoftKeyboard(BookmarksActivity.this, editText);
                new AlertDialog.Builder(BookmarksActivity.this,
                        R.style.TestpressAppCompatAlertDialogStyle)
                        .setTitle(R.string.testpress_are_you_sure)
                        .setMessage(R.string.testpress_want_to_delete_folder)
                        .setPositiveButton(R.string.testpress_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteBookmark(folder.getId(), position);
                            }
                        })
                        .setNegativeButton(R.string.testpress_no, null)
                        .show();
            }
        });
        Dialog dialog = builder.create();
        //noinspection ConstantConditions
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    void updateBookmarkFolder(long folderId, String folderName, final int position) {
        progressDialog.show();
        apiClient.updateBookmarkFolder(folderId, folderName)
                .enqueue(new TestpressCallback<BookmarkFolder>() {
                    @Override
                    public void onSuccess(BookmarkFolder folder) {
                        folderDao.updateInTx(folder);
                        updateFolderSpinnerItem(position, folder.getName());
                        progressDialog.dismiss();
                        Snackbar.make(listView, R.string.testpress_folder_updated,
                                Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        progressDialog.dismiss();
                        handleException(exception);
                    }
                });
    }

    void deleteBookmark(final Long folderId, final int deletedPosition) {
        progressDialog.show();
        apiClient.deleteBookmarkFolder(folderId)
                .enqueue(new TestpressCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        folderDao.deleteByKeyInTx(folderId);
                        List<Bookmark> bookmarks = bookmarkDao.queryBuilder()
                                .where(BookmarkDao.Properties.FolderId.eq(folderId)).list();

                        if (!bookmarks.isEmpty()) {
                            for (Bookmark bookmark : bookmarks) {
                                bookmark.setFolderId(null);
                                bookmark.setFolder(null);
                                bookmark.setLoadedInRespectiveFolder(false);
                                bookmarkDao.insertOrReplaceInTx(bookmark);
                            }
                        }

                        int currentFolderPosition = folderSpinner.getSelectedItemPosition();
                        if (currentFolderPosition == 0) { // All bookmarks
                            deleteFolderSpinnerItem(deletedPosition);
                            updateItems(false);
                        } else if (currentFolderPosition == deletedPosition) {
                            addFoldersToSpinner();
                            folderSpinner.setSelection(0);
                            folderSpinner.dismissPopUp();
                        } else {
                            String currentFolder = foldersSpinnerAdapter.getTag(currentFolderPosition);
                            deleteFolderSpinnerItem(deletedPosition);
                            if (deletedPosition < currentFolderPosition) {
                                folderSpinner.setSelection(
                                        foldersSpinnerAdapter.getItemPosition(currentFolder));

                                folderSpinner.dismissPopUp();
                            }
                        }
                        progressDialog.dismiss();
                        Snackbar.make(listView, R.string.testpress_folder_deleted,
                                Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        progressDialog.dismiss();
                        handleException(exception);
                    }
                });
    }

    void undoBookmarkDelete(final long bookmarkId, long objectId) {
        progressDialog.show();
        apiClient.undoBookmarkDelete(bookmarkId, objectId)
                .enqueue(new TestpressCallback<Bookmark>() {
                    @Override
                    public void onSuccess(Bookmark bookmark) {
                        Bookmark bookmarkFromDB = bookmarkDao.queryBuilder()
                                .where(BookmarkDao.Properties.Id.eq(bookmarkId)).list().get(0);

                        Object object = bookmarkFromDB.getBookmarkedObject();
                        if (object instanceof ReviewItem) {
                            ReviewItem reviewItem = (ReviewItem) object;
                            reviewItem.setBookmarkId(null);
                            TestpressSDKDatabase.getReviewItemDao(BookmarksActivity.this)
                                    .insertOrReplaceInTx(reviewItem);

                        } else if (object instanceof Content) {
                            Content content = (Content) object;
                            content.setBookmarkId(null);
                            TestpressSDKDatabase.getContentDao(BookmarksActivity.this)
                                    .insertOrReplaceInTx(content);
                        }
                        bookmarkFromDB.setActive(true);
                        bookmarkDao.insertOrReplaceInTx(bookmarkFromDB);
                        updateItems(true);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        progressDialog.dismiss();
                        handleException(exception);
                    }
                });
    }

    void setNavigationBarVisible(boolean visible) {
        buttonLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void refreshWithProgress() {
        refreshPager = null;
        isUserSwiped = true;
        listViewSwipeRefreshLayout.setRefreshing(true);
        viewPagerSwipeRefreshLayout.setRefreshing(true);
        refresh();
    }

    protected void refresh() {
        getSupportLoaderManager().restartLoader(REFRESH_LOADER_ID, null, this);
    }

    void handleException(TestpressException exception) {
        if(exception.isUnauthenticated()) {
            Snackbar.make(listView, R.string.testpress_authentication_failed,
                    Snackbar.LENGTH_SHORT).show();
        } else if (exception.isNetworkError()) {
            Snackbar.make(listView, R.string.testpress_no_internet_connection,
                    Snackbar.LENGTH_SHORT).show();
        } else if (exception.isClientError()) {
            Snackbar.make(listView, R.string.testpress_folder_name_not_allowed,
                    Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(listView, R.string.testpress_network_error,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (listViewSwipeRefreshLayout.getVisibility() == View.GONE && listAdapter.getCount() != 0) {
            if (isLoadingMoreView()) {
                emptyView.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.VISIBLE);
                getBookmarksListAdapter().setBackgroundShadePosition(-1);
            } else {
                getSupportFragmentManager().getFragments().get(0).setUserVisibleHint(false); // Pause web view
                getBookmarksListAdapter().setBackgroundShadePosition(viewPager.getCurrentItem());
            }
            showBookmarksList(true);
            getBookmarksListAdapter().notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }

    BookmarksListAdapter getBookmarksListAdapter() {
        return (BookmarksListAdapter) listAdapter.getWrappedAdapter();
    }

    boolean isLoadingMoreView() {
        return (viewPager.getCurrentItem() + 1) == pagerAdapter.getCount() &&
                (viewPager.getVisibility() == View.GONE || emptyView.getVisibility() == View.VISIBLE);
    }

    protected void setEmptyText(int title, int description, int imageResId, boolean hideButtonLayout) {
        if (hideButtonLayout) {
            buttonLayout.setVisibility(View.GONE);
        }
        contentLayout.setVisibility(View.GONE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
        emptyImageView.setImageResource(imageResId);
        retryButton.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Bookmark>> loader) {
    }
}
