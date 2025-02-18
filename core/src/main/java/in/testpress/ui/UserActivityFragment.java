package in.testpress.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import in.testpress.R;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.models.AccountActivity;
import in.testpress.network.AccountActivityPager;
import in.testpress.network.TestpressApiClient;
import in.testpress.util.SingleTypeAdapter;

public class UserActivityFragment extends PagedItemFragment<AccountActivity> {

    private TestpressApiClient apiClient;

    @Override
    protected int getErrorMessage(TestpressException exception) {
        return 0;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        apiClient = new TestpressApiClient(getActivity(), TestpressSdk.getTestpressSession(getContext()));
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView emptyTitleView = (TextView) view.findViewById(R.id.empty_title);
        TextView emptyDescView = (TextView) view.findViewById(R.id.empty_description);
        emptyTitleView.setText(getString(R.string.no_devices_found));
        emptyDescView.setText("");
    }

    @Override
    protected void setEmptyText() {}

    public void refreshAdapter() {
        if (getListAdapter() != null && getListAdapter().getWrappedAdapter() != null) {
            items.clear();
            getListAdapter().getWrappedAdapter().setItems(items);
            refreshWithProgress();
        }
    }

    @Override
    protected SingleTypeAdapter createAdapter(List<AccountActivity> items) {
        return new AccountActivityAdapter(getActivity().getLayoutInflater(), items, R.layout.account_activity_list_inner_content);
    }

    @Override
    protected AccountActivityPager getPager() {
        if (pager == null) {
            pager = new AccountActivityPager(apiClient);
        }
        return (AccountActivityPager) pager;
    }

}
