package in.testpress.course.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.models.Reputation;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.greendao.CourseDao;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseFragment;
import in.testpress.util.UIUtils;

import static in.testpress.course.ui.RankListFragment.PARAM_USER_REPUTATION;

public class CourseTabFragment extends BaseFragment {

    private TestpressCourseApiClient mApiClient;
    private CourseDao courseDao;
    private LinearLayout carouselView;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private LinearLayout emptyView;
    private ImageView emptyViewImage;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private ProgressBar progressBar;
    private RetrofitCall<Reputation> myReputationApiRequest;

    public static void show(FragmentActivity activity, int containerViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new CourseTabFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.testpress_fragment_carousel_with_empty_view, container, false);

        carouselView = (LinearLayout) view.findViewById(R.id.fragment_carousel);
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
        carouselView.setVisibility(View.GONE);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emptyView.setVisibility(View.GONE);
                loadMyReputation();
            }
        });
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadMyReputation();
    }

    void loadMyReputation() {
        progressBar.setVisibility(View.VISIBLE);
        myReputationApiRequest = new TestpressCourseApiClient(getContext()).getMyRank()
                .enqueue(new TestpressCallback<Reputation>() {
                    @Override
                    public void onSuccess(Reputation reputation) {
                        if (getActivity() == null) {
                            return;
                        }
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(PARAM_USER_REPUTATION, reputation);
                        CourseTabAdapter adapter = new CourseTabAdapter(getResources(),
                                getChildFragmentManager(), bundle);

                        viewPager.setAdapter(adapter);
                        tabLayout.setupWithViewPager(viewPager);
                        progressBar.setVisibility(View.GONE);
                        carouselView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onException(TestpressException exception) {
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
                            setEmptyText(R.string.testpress_error_loading_rank,
                                    R.string.testpress_some_thing_went_wrong_try_again,
                                    R.drawable.testpress_alert_warning);
                            retryButton.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    @Override
    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] { myReputationApiRequest };
    }

    private void setEmptyText(int title, int description, int imageRes) {
        progressBar.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyViewImage.setImageResource(imageRes);
        emptyDescView.setText(description);
    }

}
