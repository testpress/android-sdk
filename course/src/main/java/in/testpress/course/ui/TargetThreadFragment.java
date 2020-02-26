package in.testpress.course.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.course.R;
import in.testpress.course.models.Reputation;
import in.testpress.course.api.TestpressCourseApiClient;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseFragment;
import in.testpress.util.Assert;

import static in.testpress.course.ui.RankListFragment.PARAM_USER_REPUTATION;

public class TargetThreadFragment extends BaseFragment {

    private View emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<Reputation> reputations = new ArrayList<>();
    private Reputation userReputation;
    private RankListAdapter listAdapter;
    private RetrofitCall<TestpressApiResponse<Reputation>> targetsLoader;
    private RetrofitCall<TestpressApiResponse<Reputation>> threadsLoader;
    private TestpressCourseApiClient apiClient;

    public static void show(FragmentActivity activity, int containerViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new TargetThreadFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiClient = new TestpressCourseApiClient(getContext());
        userReputation = getArguments().getParcelable(PARAM_USER_REPUTATION);
        Assert.assertNotNull("PARAM_USER_REPUTATION must not be null.", userReputation);
        listAdapter = new RankListAdapter(getContext(),  reputations,
                userReputation.getUser().getId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.testpress_list, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reputations.clear();
                loadReputations();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.testpress_color_primary);
        emptyView = view.findViewById(R.id.empty_container);
        emptyTitleView = (TextView) view.findViewById(R.id.empty_title);
        emptyDescView = (TextView) view.findViewById(R.id.empty_description);
        retryButton = (Button) view.findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadReputations();
            }
        });
        listView.setDividerHeight(0);
        listView.setAdapter(listAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadReputations();
    }

    void loadTargets() {
        targetsLoader = apiClient.getTargets()
                .enqueue(new TestpressCallback<TestpressApiResponse<Reputation>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<Reputation> response) {
                        if (getActivity() == null) {
                            return;
                        }

                        List<Reputation> targets = response.getResults();
                        addItems(targets);
                        reputations.add(userReputation);
                        listAdapter.setStartingRank(userReputation.getRank() - targets.size());
                        loadThreads();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleErrors(exception, true);
                    }
                });
    }

    void loadThreads() {
        threadsLoader = apiClient.getThreads()
                .enqueue(new TestpressCallback<TestpressApiResponse<Reputation>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<Reputation> response) {
                        if (getActivity() == null) {
                            return;
                        }

                        addItems(response.getResults());
                        listAdapter.setItems(reputations);
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleErrors(exception, false);
                    }
                });
    }

    private void loadReputations() {
        showLoadingProgress();
        loadTargets();
    }

    private void addItems(List<Reputation> items) {
        Collections.reverse(items);
        reputations.addAll(items);
    }

    private void showLoadingProgress() {
        emptyView.setVisibility(View.GONE);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    private void handleErrors(TestpressException exception, final boolean isTarget) {
        if (getActivity() == null) {
            return;
        }
        if (exception.isNetworkError()) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again);
            if (!isTarget) {
                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showLoadingProgress();
                        loadThreads();
                    }
                });
            }
        } else if (exception.getCause() instanceof IOException) {
            setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login);
            retryButton.setVisibility(View.GONE);
        } else {
            int titleRes = isTarget ? R.string.testpress_error_loading_target :
                    R.string.testpress_error_loading_thread;

            setEmptyText(titleRes, R.string.testpress_some_thing_went_wrong_try_again);
            retryButton.setVisibility(View.GONE);
        }
    }

    private void setEmptyText(int title, int description) {
        swipeRefreshLayout.setRefreshing(false);
        if (listAdapter.getCount() != 0) {
            Snackbar.make(swipeRefreshLayout, description, Snackbar.LENGTH_SHORT).show();
        } else {
            emptyView.setVisibility(View.VISIBLE);
            emptyTitleView.setText(title);
            emptyDescView.setText(description);
            swipeRefreshLayout.setEnabled(false);
        }
    }

    @Override
    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] {
                threadsLoader, targetsLoader
        };
    }
}
