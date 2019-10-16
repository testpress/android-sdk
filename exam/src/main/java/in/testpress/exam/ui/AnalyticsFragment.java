package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import junit.framework.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.models.Subject;
import in.testpress.exam.network.SubjectPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.util.ThrowableLoader;
import in.testpress.util.UIUtils;

public class AnalyticsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Subject>> {

    public static final String ANALYTICS_URL_FRAG = "analyticsUrlFrag";
    public static final String PARENT_SUBJECT_ID = "parentSubject";
    public static final String SUBJECTS = "subjects";

    private TestpressExamApiClient apiClient;
    private LinearLayout subjectsLayout;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private LinearLayout emptyView;
    private ImageView emptyViewImage;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private ProgressBar progressBar;
    private SubjectPager pager;
    private String parentSubject;
    private String analyticsUrlFrag;
    private List<Subject> subjects = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            parentSubject = getArguments().getString(PARENT_SUBJECT_ID);
        }
        apiClient = new TestpressExamApiClient(getActivity());
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.testpress_fragment_carousel_with_empty_view, container, false);

        subjectsLayout = (LinearLayout) view.findViewById(R.id.fragment_carousel);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        emptyView = (LinearLayout) view.findViewById(R.id.empty_container);
        emptyViewImage = (ImageView) view.findViewById(R.id.image_view);
        emptyTitleView = (TextView) view.findViewById(R.id.empty_title);
        emptyDescView = (TextView) view.findViewById(R.id.empty_description);
        emptyTitleView.setTypeface(TestpressSdk.getRubikMediumFont(getContext()));
        emptyDescView.setTypeface(TestpressSdk.getRubikRegularFont(getContext()));
        retryButton = (Button) view.findViewById(R.id.retry_button);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_loading);
        UIUtils.setIndeterminateDrawable(getActivity(), progressBar, 4);
        subjectsLayout.setVisibility(View.GONE);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
                getLoaderManager().restartLoader(0, null, AnalyticsFragment.this);
            }
        });
        return view;
    }

    private SubjectPager getPager() {
        if (pager == null) {
            analyticsUrlFrag = getArguments().getString(ANALYTICS_URL_FRAG);
            Assert.assertNotNull("ANALYTICS_URL_FRAG must not be null", analyticsUrlFrag);
            if (parentSubject != null) {
                pager = new SubjectPager(parentSubject, analyticsUrlFrag, apiClient);
            } else {
                pager = new SubjectPager("null", analyticsUrlFrag, apiClient);
            }
        }
        return pager;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<List<Subject>> onCreateLoader(int id, final Bundle args) {
        return new ThrowableLoader<List<Subject>>(getActivity(), subjects) {
            @Override
            public List<Subject> loadData() throws TestpressException {
                do {
                    getPager().next();
                    subjects = pager.getResources();
                } while (pager.hasNext());
                return subjects;
            }
        };
    }

    @SuppressLint("InflateParams")
    @Override
    public void onLoadFinished(final Loader<List<Subject>> loader, final List<Subject> items) {
        //noinspection ThrowableResultOfMethodCallIgnored
        TestpressException exception = ((ThrowableLoader<List<Subject>>) loader).clearException();
        getLoaderManager().destroyLoader(loader.getId());
        if(exception != null) {
            if (exception.isNetworkError()) {
                setEmptyText(R.string.testpress_network_error,
                        R.string.testpress_no_internet_try_again,
                        R.drawable.testpress_no_wifi);
            } else if (exception.getCause() instanceof IOException) {
                setEmptyText(R.string.testpress_authentication_failed,
                        R.string.testpress_please_login,
                        R.drawable.testpress_alert_warning);
                retryButton.setVisibility(View.INVISIBLE);
            } else {
                setEmptyText(R.string.testpress_error_loading_analytics,
                        R.string.testpress_some_thing_went_wrong_try_again,
                        R.drawable.testpress_alert_warning);
            }
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (items.isEmpty()) {
            setEmptyText(R.string.testpress_no_analytics,
                    R.string.testpress_no_subjects_description,
                    R.drawable.testpress_analytics);
            retryButton.setVisibility(View.INVISIBLE);
            if (parentSubject != null) {
                emptyDescView.setVisibility(View.GONE);
            } else if (analyticsUrlFrag.contains(TestpressExamApiClient.SUBJECT_ANALYTICS_PATH)) {
                emptyDescView.setText(R.string.testpress_no_analytics_description);
            }
            progressBar.setVisibility(View.GONE);
            return;
        }

        emptyView.setVisibility(View.GONE);
        subjectsLayout.setVisibility(View.VISIBLE);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(SUBJECTS, new ArrayList<>(items));
        bundle.putString(ANALYTICS_URL_FRAG, analyticsUrlFrag);
        AnalyticsPagerAdapter adapter = new AnalyticsPagerAdapter(getResources(),
                getChildFragmentManager(), bundle);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        progressBar.setVisibility(View.GONE);
    }

    private void setEmptyText(int title, int description, int imageRes) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyViewImage.setImageResource(imageRes);
        emptyDescView.setText(description);
    }

    @Override
    public void onLoaderReset(final Loader<List<Subject>> loader) {
    }

}
