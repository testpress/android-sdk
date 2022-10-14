package in.testpress.course.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.exam.api.TestpressExamApiClient;
import in.testpress.exam.ui.EditableItemSpinnerAdapter;
import in.testpress.models.greendao.Bookmark;
import in.testpress.models.greendao.BookmarkDao;
import in.testpress.models.greendao.BookmarkFolder;
import in.testpress.models.greendao.BookmarkFolderDao;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ReviewItem;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.ui.view.ClosableSpinner;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.FolderListResponse;

import static in.testpress.exam.api.TestpressExamApiClient.BOOKMARK_FOLDERS_PATH;
import static in.testpress.models.greendao.BookmarkFolder.UNCATEGORIZED;

public class BookmarksActivity extends BaseToolBarActivity  {

    // Loader for refresh
    private static final int REFRESH_LOADER_ID = 0;

    // Loader to load bottom old bookmarks
    private static final int LOADER_ID = 1;

    private BookmarkListFragment fragment;

    private SwipeRefreshLayout listViewSwipeRefreshLayout;
    private View contentLayout;
    View buttonLayout;
    private Button retryButton;
    private Button previousButton;
    private Button nextButton;
    private ClosableSpinner folderSpinner;
    private LinearLayout newBookmarksAvailableLabel;
    private EditableItemSpinnerAdapter foldersSpinnerAdapter;
    private View loadingLayout;
    private TestpressExamApiClient apiClient;
    private String currentFolder = "";
    private BookmarkFolderDao folderDao;
    private BookmarkDao bookmarkDao;
    private List<BookmarkFolder> bookmarkFolders = new ArrayList<>();
    private ProgressDialog progressDialog;
    private RetrofitCall<ApiResponse<FolderListResponse>> bookmarkFoldersLoader;
    private RetrofitCall<BookmarkFolder> updateFolderAPIRequest;
    private RetrofitCall<Void> deleteFolderAPIRequest;
    private RetrofitCall<Bookmark> undoBookmarkAPIRequest;

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_bookmarks);

        loadBookmarks();
        retryButton = findViewById(R.id.retry_button);
        previousButton = findViewById(R.id.previous);
        nextButton = findViewById(R.id.next);
        contentLayout = findViewById(R.id.content_layout);
        buttonLayout = findViewById(R.id.button_layout);
        newBookmarksAvailableLabel = findViewById(R.id.new_bookmarks_available_label);
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
                new TextView[] { nextButton, previousButton},
                TestpressSdk.getRubikMediumFont(this)
        );

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonLayout.getVisibility() == View.VISIBLE) {
                    contentLayout.setVisibility(View.VISIBLE);
                } else {
                    refreshWithProgress();
                }
            }
        });
        contentLayout.setVisibility(View.VISIBLE);
        listViewSwipeRefreshLayout = findViewById(R.id.list_view_swipe_container);
        listViewSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWithProgress();
            }
        });
        listViewSwipeRefreshLayout.setColorSchemeResources(R.color.testpress_color_primary);

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

                Loader<Bookmark> refreshLoader =
                        getSupportLoaderManager().getLoader(REFRESH_LOADER_ID);

                if (refreshLoader != null && !refreshLoader.isReset()) {
                    getSupportLoaderManager().destroyLoader(REFRESH_LOADER_ID);
                }
                Loader<Bookmark> loader = getSupportLoaderManager().getLoader(LOADER_ID);
                if (loader != null && !loader.isReset()) {
                    getSupportLoaderManager().destroyLoader(LOADER_ID);
                }
                }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void loadBookmarks() {
        fragment =new BookmarkListFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.bookmark_fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    void loadBookmarkFolders(String url) {
        bookmarkFoldersLoader = apiClient.getBookmarkFolders(url)
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
        foldersSpinnerAdapter.addItem("", "All Bookmarks", 0);
        List<BookmarkFolder> bookmarkFolders = folderDao.queryBuilder().list();
        for (BookmarkFolder folder: bookmarkFolders) {
            foldersSpinnerAdapter
                    .addItem(folder.getName(), folder.getName(), folder.getBookmarksCount());
        }
        foldersSpinnerAdapter.addItem(UNCATEGORIZED, UNCATEGORIZED, 0);
        foldersSpinnerAdapter.notifyDataSetChanged();
    }

    void addNewFolderToSpinner(String folderName) {
        String currentFolder = foldersSpinnerAdapter.getTag(folderSpinner.getSelectedItemPosition());
        int index = foldersSpinnerAdapter.getCount() - 1;
        foldersSpinnerAdapter.addItem(index, folderName, folderName, 1);
        foldersSpinnerAdapter.notifyDataSetChanged();
        folderSpinner.setSelection(foldersSpinnerAdapter.getItemPosition(currentFolder));
    }

    void updateFolderSpinnerItem(BookmarkFolder folder) {
        updateFolderSpinnerItem(foldersSpinnerAdapter.getItemPosition(folder.getName()), folder);
    }

    void updateFolderSpinnerItem(int position, BookmarkFolder folder) {
        foldersSpinnerAdapter.updateItem(position, folder.getName(), folder.getName(),
                folder.getBookmarksCount());

        foldersSpinnerAdapter.notifyDataSetChanged();
    }

    void deleteFolderSpinnerItem(int position) {
        foldersSpinnerAdapter.removeItem(position);
        foldersSpinnerAdapter.notifyDataSetChanged();
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
                UIUtils.hideSoftKeyboard(in.testpress.course.ui.BookmarksActivity.this, editText);
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
                UIUtils.hideSoftKeyboard(in.testpress.course.ui.BookmarksActivity.this, editText);
                new AlertDialog.Builder(in.testpress.course.ui.BookmarksActivity.this,
                        R.style.TestpressAppCompatAlertDialogStyle)
                        .setTitle(R.string.testpress_are_you_sure)
                        .setMessage(R.string.testpress_want_to_delete_folder)
                        .setPositiveButton(R.string.testpress_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteFolder(folder.getId(), position);
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
        updateFolderAPIRequest = apiClient.updateBookmarkFolder(folderId, folderName)
                .enqueue(new TestpressCallback<BookmarkFolder>() {
                    @Override
                    public void onSuccess(BookmarkFolder folder) {
                        folderDao.updateInTx(folder);
                        updateFolderSpinnerItem(position, folder);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        progressDialog.dismiss();
                        handleException(exception);
                    }
                });
    }

    void deleteFolder(final Long folderId, final int deletedPosition) {
        progressDialog.show();
        deleteFolderAPIRequest = apiClient.deleteBookmarkFolder(folderId)
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
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        progressDialog.dismiss();
                        handleException(exception);
                    }
                });
    }

    void undoBookmarkDelete(final long bookmarkId) {
        progressDialog.show();
        Bookmark bookmark = bookmarkDao.queryBuilder()
                .where(BookmarkDao.Properties.Id.eq(bookmarkId)).list().get(0);

        undoBookmarkAPIRequest = apiClient
                .undoBookmarkDelete(bookmarkId, bookmark.getObjectId(), bookmark.getFolderFromDB())
                .enqueue(new TestpressCallback<Bookmark>() {
                    @Override
                    public void onSuccess(Bookmark bookmark) {
                        Bookmark bookmarkFromDB = bookmarkDao.queryBuilder()
                                .where(BookmarkDao.Properties.Id.eq(bookmarkId)).list().get(0);

                        Object object = bookmarkFromDB.getBookmarkedObject();
                        if (object instanceof ReviewItem) {
                            ReviewItem reviewItem = (ReviewItem) object;
                            reviewItem.setBookmarkId(bookmarkId);
                            TestpressSDKDatabase.getReviewItemDao(in.testpress.course.ui.BookmarksActivity.this)
                                    .insertOrReplaceInTx(reviewItem);

                        } else if (object instanceof Content) {
                            Content content = (Content) object;
                            content.setBookmarkId(bookmarkId);
                            TestpressSDKDatabase.getContentDao(in.testpress.course.ui.BookmarksActivity.this)
                                    .insertOrReplaceInTx(content);
                        }
                        bookmarkFromDB.setActive(true);
                        bookmarkDao.insertOrReplaceInTx(bookmarkFromDB);
                        if (bookmark.getFolderId() != null) {
                            BookmarkFolder folder = folderDao.queryBuilder()
                                    .where(BookmarkFolderDao.Properties.Id.eq(bookmark.getFolderId()))
                                    .list().get(0);

                            folder.setBookmarksCount(folder.getBookmarksCount() + 1);
                            folderDao.insertOrReplaceInTx(folder);
                            updateFolderSpinnerItem(folder);
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        progressDialog.dismiss();
                        handleException(exception);
                    }
                });
    }

    public void refreshWithProgress() {

        fragment.onRefreshing();
        listViewSwipeRefreshLayout.setRefreshing(false);
        contentLayout.setVisibility(View.VISIBLE);

    }

    void handleException(TestpressException exception) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    protected void setEmptyText(int title, int description, int imageResId, boolean hideButtonLayout) {
        if (hideButtonLayout) {
            buttonLayout.setVisibility(View.GONE);
        }
        contentLayout.setVisibility(View.GONE);

        retryButton.setVisibility(View.VISIBLE);
    }

    @Override
    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] {
                bookmarkFoldersLoader, updateFolderAPIRequest, deleteFolderAPIRequest,
                undoBookmarkAPIRequest
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        fragment.onRefreshing();
    }
}

