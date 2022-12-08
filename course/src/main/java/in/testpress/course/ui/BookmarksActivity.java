package in.testpress.course.ui;

import static in.testpress.exam.api.TestpressExamApiClient.BOOKMARK_FOLDERS_PATH;
import static in.testpress.models.greendao.BookmarkFolder.UNCATEGORIZED;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.repository.BookmarkFolderRepository;
import in.testpress.course.viewmodels.BookmarkFolderViewModel;
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

public class BookmarksActivity extends BaseToolBarActivity {

    // Loader for refresh
    private static final int REFRESH_LOADER_ID = 0;

    // Loader to load bottom old bookmarks
    private static final int LOADER_ID = 1;

    private BookmarkListFragment fragment;

    private BookmarkFolderViewModel bookmarkFolderViewModel;

    private String TAG = "BookmarksActivity";
    private String baseUrl;

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
        apiClient = new TestpressExamApiClient(this);

        initializeViewModel();
        initalizeObservers();
        loadBookmarks();

        baseUrl = TestpressSdk.getTestpressSession(getApplicationContext())
                .getInstituteSettings()
                .getBaseUrl();

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

        bookmarkDao = TestpressSDKDatabase.getBookmarkDao(this);

        ViewUtils.setTypeface(
                new TextView[]{nextButton, previousButton},
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
                        folderSpinner.setSelection(position);
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
        loadBookmarkFolders();
        folderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentFolder = foldersSpinnerAdapter.getTag(i);
                Log.d(TAG, "onItemSelected: "+currentFolder);
                for (BookmarkFolder folder:
                     bookmarkFolders) {
                    if(currentFolder.equals("")){
                        fragment.setFolderID(0L);
                        return;
                    }
                    if (folder.getName().equals(currentFolder)){
                        fragment.setFolderID(folder.getId());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void initializeViewModel() {
        ViewModelProvider.Factory viewModelFactory = new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> aClass) {
                return (T) new BookmarkFolderViewModel(
                        new BookmarkFolderRepository(
                                getApplicationContext(),
                                apiClient)
                );
            }
        };
        bookmarkFolderViewModel = new ViewModelProvider(getViewModelStore(), viewModelFactory).get(BookmarkFolderViewModel.class);

    }

    void initalizeObservers(){
        bookmarkFolderViewModel.getFolders().observe(this, folders ->{
            switch (folders.getStatus()){
                case SUCCESS:{
                    Log.d(TAG, "onCreate: "+folders.getData().size());
                    bookmarkFolders  = folders.getData();
                    addFoldersToSpinner();
                    break;
                }
                case LOADING:{

                    break;
                }
                case ERROR:{
                    handleException(folders.getException());
                    break;
                }
            }
        });

        bookmarkFolderViewModel.getUpdateFolder().observe(this,updateFolder ->{
            switch (updateFolder.getStatus()){
                case SUCCESS:{
                    int currentFolderPosition = folderSpinner.getSelectedItemPosition();
                        updateFolderSpinnerItem(currentFolderPosition, Objects.requireNonNull(updateFolder.getData()));
                        progressDialog.dismiss();
                        Snackbar.make(fragment.requireView(), R.string.testpress_folder_updated,
                                Snackbar.LENGTH_SHORT).show();
                    break;
                }
                case LOADING:{
                    Log.d(TAG, "updateBookmarkFolder: loading");
                    progressDialog.show();
                    break;
                }
                case ERROR:{
                    Log.d(TAG, "updateBookmarkFolder: error");
                    handleException(updateFolder.getException());
                    progressDialog.dismiss();
                    break;
                }
            }
        });

        bookmarkFolderViewModel.getDeleteFolder().observe(this, result -> {
            Log.d(TAG, "deleteFolder: "+result.getStatus());
            switch (result.getStatus()){
                case SUCCESS:{
                    int currentFolderPosition = folderSpinner.getSelectedItemPosition();
                    deleteFolderSpinnerItem(currentFolderPosition);
                    folderSpinner.setSelection(0);
                    progressDialog.dismiss();
                    Snackbar.make(fragment.requireView(), R.string.testpress_folder_deleted,
                            Snackbar.LENGTH_SHORT).show();
                    break;
                }
                case ERROR:{
                    progressDialog.dismiss();
                    handleException(result.getException());
                    break;
                }
            }
        });


    }

    private void loadBookmarks() {
        fragment = new BookmarkListFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.bookmark_fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    void loadBookmarkFolders(){
        bookmarkFolderViewModel.loadFolders(baseUrl+ BOOKMARK_FOLDERS_PATH);
    }

    synchronized void addFoldersToSpinner() {
        foldersSpinnerAdapter.clear();
        foldersSpinnerAdapter.addItem("", "All Bookmarks", 0);
        for (BookmarkFolder folder : bookmarkFolders) {
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
        fragment.setFolderID(folder.getId());
    }

    void updateFolderSpinnerItem(int position, @NonNull BookmarkFolder folder) {
        Log.d(TAG, "updateFolderSpinnerItem: "+position);
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

    void updateBookmarkFolder(long folderId, String folderName, int position) {
        progressDialog.show();
        bookmarkFolderViewModel.updateFolder(folderId,folderName);
    }

    void deleteFolder(final Long folderId, final int deletedPosition) {
        progressDialog.show();
        bookmarkFolderViewModel.deleteFolder(folderId);
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
        loadBookmarkFolders();
        listViewSwipeRefreshLayout.setRefreshing(false);
        contentLayout.setVisibility(View.VISIBLE);
    }

    void handleException(TestpressException exception) {
        if (exception.isUnauthenticated()) {
            Snackbar.make(fragment.requireView(), R.string.testpress_authentication_failed,
                    Snackbar.LENGTH_SHORT).show();
        } else if (exception.isNetworkError()) {
            Snackbar.make(fragment.requireView(), R.string.testpress_no_internet_connection,
                    Snackbar.LENGTH_SHORT).show();
        } else if (exception.isClientError()) {
            Snackbar.make(fragment.requireView(), R.string.testpress_folder_name_not_allowed,
                    Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(fragment.requireView(), R.string.testpress_network_error,
                    Snackbar.LENGTH_SHORT).show();
        }
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
        return new RetrofitCall[]{
                bookmarkFoldersLoader, updateFolderAPIRequest, deleteFolderAPIRequest,
                undoBookmarkAPIRequest
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        fragment.onRefreshing();
        loadBookmarkFolders();
    }
}

