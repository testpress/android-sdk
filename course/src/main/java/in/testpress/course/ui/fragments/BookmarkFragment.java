package in.testpress.course.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.ui.FolderSpinnerAdapter;
import in.testpress.models.greendao.Bookmark;
import in.testpress.models.greendao.BookmarkFolder;
import in.testpress.ui.view.ClosableSpinner;
import in.testpress.util.ViewUtils;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.FolderListResponse;

import static in.testpress.exam.network.TestpressExamApiClient.BOOKMARK_FOLDERS_PATH;
import static in.testpress.models.greendao.BookmarkFolder.UNCATEGORIZED;


public class BookmarkFragment extends Fragment {
    private LottieAnimationView animationView;
    private TextView bookmarkButtonText;
    private ImageView bookmarkButtonImage;
    private RelativeLayout bookmarkLayout;
    private LinearLayout bookmarkButtonLayout;
    private ArrayList<BookmarkFolder> bookmarkFolders = new ArrayList<>();
    private ClosableSpinner bookmarkFolderSpinner;
    private FolderSpinnerAdapter folderSpinnerAdapter;

    private TestpressExamApiClient examApiClient;
    private BookmarkListener bookmarkListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bookmark_layout, container, false);
    }

    public void setBookmarkListener(BookmarkListener bookmarkListener) {
        this.bookmarkListener = bookmarkListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        examApiClient = new TestpressExamApiClient(getActivity());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
                if (bookmarkListener.getBookmarkId() != null) {
                    deleteBookmark(bookmarkListener.getBookmarkId());
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

        boolean bookmarksEnabled = TestpressSdk.getTestpressSession(getActivity()).getInstituteSettings()
                .isBookmarksEnabled();

        if (bookmarksEnabled && bookmarkListener != null) {
            if (bookmarkListener.getBookmarkId() != null) {
                bookmarkButtonText.setText(R.string.testpress_remove_bookmark);
                bookmarkButtonImage.setImageResource(R.drawable.ic_remove_bookmark);
            } else {
                bookmarkButtonText.setText(R.string.testpress_bookmark_this);
                bookmarkButtonImage.setImageResource(R.drawable.ic_bookmark);
            }
            bookmarkLayout.setVisibility(View.VISIBLE);
        } else {
            bookmarkLayout.setVisibility(View.GONE);
        }
    }


    void bookmark(String folder) {
        setBookmarkProgress(true);
        examApiClient.bookmark(bookmarkListener.getContentId(), folder, "chaptercontent", "courses")
                .enqueue(new TestpressCallback<Bookmark>() {
                    @Override
                    public void onSuccess(Bookmark bookmark) {
                        bookmarkListener.onBookmarkSuccess(bookmark.getId());
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
                        bookmarkListener.onDeleteBookmarkSuccess();
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


    void handleException(TestpressException exception) {
        if(exception.isUnauthenticated()) {
            Snackbar.make(bookmarkLayout, R.string.testpress_authentication_failed,
                    Snackbar.LENGTH_SHORT).show();
        } else if (exception.getCause() instanceof IOException) {
            Snackbar.make(bookmarkLayout, R.string.testpress_no_internet_connection,
                    Snackbar.LENGTH_SHORT).show();
        } else if (exception.isClientError()) {
            Snackbar.make(bookmarkLayout, R.string.testpress_folder_name_not_allowed,
                    Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(bookmarkLayout, R.string.testpress_network_error,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    public interface BookmarkListener {
        Long getBookmarkId();
        Long getContentId();
        void onBookmarkSuccess(Long bookmarkId);
        void onDeleteBookmarkSuccess();
    }

}
