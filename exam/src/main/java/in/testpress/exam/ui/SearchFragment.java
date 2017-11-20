package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.exam.R;
import in.testpress.models.greendao.Exam;
import in.testpress.exam.network.ExamPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.models.greendao.ExamDao;
import in.testpress.models.greendao.TestpressSDK;
import in.testpress.ui.HeaderFooterListAdapter;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ThrowableLoader;
import in.testpress.util.ViewUtils;
import in.testpress.util.UIUtils;

public class SearchFragment extends Fragment implements AbsListView.OnScrollListener,
        LoaderManager.LoaderCallbacks<List<Exam>> {

    public static final String SUBCLASS = "subclass";
    public static final String CATEGORY = "category";
    private static final int SPEECH_RESULT = 111;
    private ProgressBar progressBar;
    private EditText searchBar;
    private ImageView leftDrawable;
    private ImageView rightDrawable;
    private CardView resultsLayout;
    private LinearLayout emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private ListView listView;
    private View searchLayout;
    private String queryText = "";
    private String subclass = "";
    private  List<Exam> items = Collections.emptyList();
    private  ExamPager pager;
    private View loadingLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subclass = getArguments().getString(SUBCLASS);
        String category = getArguments().getString(CATEGORY);
        pager = new ExamPager(subclass, category, new TestpressExamApiClient(getActivity()));
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.testpress_fragment_search,
                container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_loading);
        UIUtils.setIndeterminateDrawable(getActivity(), progressBar, 4);
        searchBar = (EditText) view.findViewById(R.id.search_bar);
        leftDrawable = (ImageView) view.findViewById(R.id.left_drawable);
        rightDrawable = (ImageView) view.findViewById(R.id.right_drawable);
        resultsLayout = (CardView) view.findViewById(R.id.result_list_card);
        emptyView = (LinearLayout) view.findViewById(R.id.empty_container);
        emptyTitleView = (TextView) view.findViewById(R.id.empty_title);
        emptyDescView = (TextView) view.findViewById(R.id.empty_description);
        retryButton = (Button) view.findViewById(R.id.retry_button);
        listView = (ListView) view.findViewById(android.R.id.list);
        searchLayout = view;
        view.findViewById(R.id.left_drawable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchBar.getText().toString().isEmpty()) {
                    //Work as back arrow
                    getActivity().finish();
                } else {
                    // Work as search icon
                    onClickSearch();
                }
            }
        });
        view.findViewById(R.id.right_drawable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickRightDrawable();
            }
        });
        view.findViewById(R.id.retry_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshWithProgress();
            }
        });
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (searchBar.getText().toString().isEmpty()) {
                    leftDrawable.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                            R.drawable.ic_arrow_back_black_24dp));
                    if (!SpeechRecognizer.isRecognitionAvailable(getActivity())) {
                        rightDrawable.setVisibility(View.GONE);
                    } else {
                        rightDrawable.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                                R.drawable.ic_mic_black_24dp));
                    }
                } else {
                    if (rightDrawable.getVisibility() == View.GONE) {
                        rightDrawable.setVisibility(View.VISIBLE);
                    }
                    rightDrawable.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                            R.drawable.ic_close_black_24dp));
                    leftDrawable.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                            R.drawable.ic_search_white_24dp));
                }
            }
        });
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    onClickSearch();
                    return true;
                }
                return false;
            }
        });
        listView.setAdapter(createAdapter());
        loadingLayout = LayoutInflater.from(getActivity()).inflate(R.layout.testpress_loading_layout, null);
        listView.setOnScrollListener(this);
        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);
        if (!SpeechRecognizer.isRecognitionAvailable(getActivity())) {
            rightDrawable.setVisibility(View.GONE);
        }
        return view;
    }

    private void onClickSearch() {
        queryText = searchBar.getText().toString().trim();
        if (queryText.length() != 0) {
            ViewUtils.hideSoftKeyboard(getActivity());
            refreshWithProgress();
        }
    }

    private void onClickRightDrawable() {
        if (searchBar.getText().toString().isEmpty()) {
            //Work as speech recognizer
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
            try {
                startActivityForResult(intent, SPEECH_RESULT);
                searchBar.setText("");
            } catch (ActivityNotFoundException a) {
                Snackbar.make(searchLayout, R.string.testpress_speech_recognition_not_supported,
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            //Work as clear button
            if (!SpeechRecognizer.isRecognitionAvailable(getActivity())) {
                rightDrawable.setVisibility(View.GONE);
            } else {
                rightDrawable.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                        R.drawable.ic_mic_black_24dp));
            }
            searchBar.setText("");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SPEECH_RESULT == requestCode && resultCode == Activity.RESULT_OK && null != data) {
            searchBar.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
            searchBar.setSelection(searchBar.getText().length());
            searchBar.requestFocus();
            onClickSearch();
        } else if ((requestCode == CarouselFragment.TEST_TAKEN_REQUEST_CODE) &&
                (Activity.RESULT_OK == resultCode)) {
            getActivity().setResult(resultCode);
            getActivity().finish();
        }
    }

    @Override
    public Loader<List<Exam>> onCreateLoader(int id, Bundle args) {
        return new ThrowableLoader<List<Exam>>(getActivity(), items) {

            @Override
            public List<Exam> loadData() throws TestpressException {
                pager.setQueryParams(TestpressExamApiClient.SEARCH_QUERY, queryText);
                pager.next();
                return pager.getResources();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Exam>> loader, List<Exam> items) {
        if (pager != null && !pager.hasMore()) {
            if(getListAdapter().getFootersCount() != 0) {
                // If pager reached last page remove footer if footer added already
                getListAdapter().removeFooter(loadingLayout);
            }
        }
        final TestpressException exception = getException(loader);
        if (exception != null) {
            if (!items.isEmpty()) {
                Snackbar.make(searchLayout, getErrorMessage(exception), Snackbar.LENGTH_LONG).show();
            }
            setListShown(true);
            return;
        }
        if (items.isEmpty()) {
            setEmptyText(R.string.testpress_no_results_found, R.string.testpress_try_with_other_keyword,
                    R.drawable.ic_error_outline_black_18dp);
            retryButton.setVisibility(View.GONE);
        }
        this.items = items;
        getListAdapter().getWrappedAdapter().setItems(items.toArray());
        setListShown(true);
    }

    @Override
    public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount, int totalItemCount)
    {
        if (pager != null && !pager.hasMore()) {
            if(getListAdapter().getFootersCount() != 0) {
                // If pager reached last page remove footer if footer added already
                getListAdapter().removeFooter(loadingLayout);
            }
            return;
        }
        if (getLoaderManager().hasRunningLoaders())
            return;
        if (listView != null && pager != null
                && (listView.getLastVisiblePosition() + 3) >= pager.size()) {
            if(getListAdapter().getFootersCount() == 0) {
                // Display loading footer if not present when loading next page
                getListAdapter().addFooter(loadingLayout);
            }
            refresh();
        }
    }

    private void refreshWithProgress() {
        if (pager != null) {
            pager.reset();
        }
        items.clear();
        setListShown(false);
        refresh();
    }

    private void refresh() {
        getLoaderManager().restartLoader(0, null, this);
    }

    private HeaderFooterListAdapter<SingleTypeAdapter<Exam>> createAdapter() {
        SingleTypeAdapter<Exam> wrapped = null;
        ExamDao examDao = TestpressSDK.getExamDao(getContext());
        if (subclass == null || subclass.equals("available")) {
            wrapped = new AvailableExamsListAdapter(this, items, examDao);
        } else if (subclass.equals("upcoming")) {
            wrapped = new UpcomingExamsListAdapter(getActivity(), items, examDao);
        } else if (subclass.equals("history")) {
            wrapped = new HistoryListAdapter(this, items, examDao);
        }
        return new HeaderFooterListAdapter<SingleTypeAdapter<Exam>>(listView, wrapped);
    }

    private void setListShown(final boolean shown) {
        if (shown) {
            if (!items.isEmpty()) {
                hide(progressBar).hide(emptyView).show(resultsLayout);
            } else {
                hide(progressBar).hide(resultsLayout).show(emptyView);
            }
        } else {
            hide(resultsLayout).hide(emptyView).show(progressBar);
        }
    }

    private int getErrorMessage(TestpressException exception) {
        if(exception.isUnauthenticated()) {
            return R.string.testpress_authentication_failed;
        } else if (exception.isNetworkError()) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_no_internet_try_again;
        } else {
            setEmptyText(R.string.testpress_error_loading_exams,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_error_loading_exams;
    }

    private SearchFragment show(final View view) {
        ViewUtils.setGone(view, false);
        return this;
    }

    private SearchFragment hide(final View view) {
        ViewUtils.setGone(view, true);
        return this;
    }

    private HeaderFooterListAdapter<SingleTypeAdapter<Exam>> getListAdapter() {
        return (HeaderFooterListAdapter<SingleTypeAdapter<Exam>>) listView
                .getAdapter();
    }

    private TestpressException getException(final Loader<List<Exam>> loader) {
        if (loader instanceof ThrowableLoader) {
            return ((ThrowableLoader<List<Exam>>) loader).clearException();
        } else {
            return null;
        }
    }

    private void setEmptyText(final int title, final int description, final int left) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<List<Exam>> loader) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

}
