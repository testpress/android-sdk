package in.testpress.course.ui;

import android.app.Activity;
import android.widget.ListView;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.course.R;
import in.testpress.course.models.Reputation;
import in.testpress.course.network.LeaderboardPager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.network.BaseResourcePager;
import in.testpress.ui.PagedItemFragment;
import in.testpress.util.Assert;
import in.testpress.util.SingleTypeAdapter;

public class RankListFragment extends PagedItemFragment<Reputation> {

    public static final String PARAM_USER_REPUTATION = "userReputation";

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);
        listView.setDividerHeight(0);
    }

    @Override
    protected BaseResourcePager<Reputation> getPager() {
        if (pager == null) {
            pager = new LeaderboardPager(new TestpressCourseApiClient(getContext()));
        }
        return pager;
    }

    @Override
    protected int getErrorMessage(TestpressException exception) {
        if (exception.isUnauthenticated()) {
            setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_authentication_failed;
        } else if (exception.isNetworkError()) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_no_internet_try_again;
        }
        setEmptyText(R.string.testpress_error_loading_leaderboard,
                R.string.testpress_some_thing_went_wrong_try_again,
                R.drawable.ic_error_outline_black_18dp);
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_leaderboard_not_available,
                R.string.testpress_leaderboard_not_available_description,
                R.drawable.ic_error_outline_black_18dp);
    }

    @Override
    protected SingleTypeAdapter<Reputation> createAdapter(List<Reputation> reputations) {
        Reputation userReputation = getArguments().getParcelable(PARAM_USER_REPUTATION);
        Assert.assertNotNull("PARAM_USER_REPUTATION must not be null.", userReputation);
        return new RankListAdapter(getContext(),  reputations, userReputation.getUser().getId());
    }

}
