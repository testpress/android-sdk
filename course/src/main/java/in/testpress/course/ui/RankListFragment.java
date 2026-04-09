package in.testpress.course.ui;

import android.app.Activity;
import android.widget.ListView;
import org.json.JSONObject;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.course.R;
import in.testpress.course.TestpressCourse;
import in.testpress.course.models.Reputation;
import in.testpress.course.pagers.LeaderboardPager;
import in.testpress.course.api.TestpressCourseApiClient;
import in.testpress.network.BaseResourcePager;
import in.testpress.ui.PagedItemFragment;
import in.testpress.util.SingleTypeAdapter;

import static in.testpress.course.api.TestpressCourseApiClient.COURSE_ID;

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
        String courseId = getArguments().getString(TestpressCourse.COURSE_ID);
        if (courseId != null && !courseId.isEmpty()) {
            pager.setQueryParams(COURSE_ID, courseId);
        }
        return pager;
    }

    @Override
    public void onLoadFinished(final androidx.loader.content.Loader<List<Reputation>> loader, final List<Reputation> reputations) {
        final TestpressException exception = getException(loader);
        getLoaderManager().destroyLoader(loader.getId());

        if (exception != null) {
            this.exception = exception;

            if (exception.isForbidden()) {
                String message = getForbiddenMessage(exception);
                if (message == null || message.trim().isEmpty()) {
                    setEmptyText(R.string.permission_denied, R.string.testpress_no_permission,
                            R.drawable.ic_error_outline_black_18dp);
                } else {
                    setEmptyText(R.string.permission_denied, message, R.drawable.ic_error_outline_black_18dp);
                }
                items.clear();
                getListAdapter().getWrappedAdapter().setItems(items.toArray());
                retryButton.setVisibility(android.view.View.GONE);
                showList();
                return;
            }

            int errorMessage = getErrorMessage(exception);
            if (!reputations.isEmpty()) {
                showError(errorMessage);
            }
            showList();
            return;
        }

        this.exception = null;
        updateItems(reputations);
    }

    private String getForbiddenMessage(TestpressException exception) {
        try {
            String raw = exception.getErrorBodyString();
            if (raw == null || raw.trim().isEmpty()) {
                return null;
            }
            JSONObject json = new JSONObject(raw);
            if (json.has("message")) {
                return json.getString("message");
            }
            if (json.has("detail")) {
                return json.getString("detail");
            }
        } catch (Exception ignore) {}
        return null;
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
        if (userReputation == null) {
            return new RankListAdapter(getContext(), reputations);
        }
        return new RankListAdapter(getContext(),  reputations, userReputation.getUser().getId());
    }

}
