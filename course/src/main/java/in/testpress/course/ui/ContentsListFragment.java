package in.testpress.course.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.course.R;
import in.testpress.course.models.Content;
import in.testpress.course.network.ContentPager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.ui.PagedItemFragment;
import in.testpress.util.SingleTypeAdapter;

import static in.testpress.course.ui.ContentsListActivity.CONTENTS_URL_FRAG;

public class ContentsListFragment extends PagedItemFragment<Content> {

    private TestpressCourseApiClient mApiClient;
    private String contentsUrlFrag;

    public static void show(FragmentActivity activity, int containerViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new ContentsListFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentsUrlFrag =  getArguments().getString(CONTENTS_URL_FRAG);
        if (getArguments() == null || contentsUrlFrag == null || contentsUrlFrag.isEmpty()) {
            throw new IllegalArgumentException("CONTENTS_URL_FRAG must not be null or empty");
        }
        mApiClient = new TestpressCourseApiClient(getActivity());
    }

    @Override
    protected ContentPager getPager() {
        if (pager == null) {
            pager = new ContentPager(contentsUrlFrag, mApiClient);
        }
        return (ContentPager)pager;
    }

    @Override
    protected SingleTypeAdapter<Content> createAdapter(List<Content> items) {
        return new ContentsListAdapter(getActivity(), items, R.layout.testpress_content_list_item);
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
        } else {
            setEmptyText(R.string.testpress_error_loading_contents,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_content, R.string.testpress_no_content_description,
                    R.drawable.ic_error_outline_black_18dp);
    }

}
