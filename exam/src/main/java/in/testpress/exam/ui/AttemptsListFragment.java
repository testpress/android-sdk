package in.testpress.exam.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import java.io.IOException;
import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.Exam;
import in.testpress.exam.network.AttemptsPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.util.SingleTypeAdapter;

public class AttemptsListFragment extends PagedItemFragment<Attempt> {

    static final String PARAM_EXAM = "exam";
    static final String PARAM_STATE = "state";
    static final String STATE_PAUSED = "paused";
    private Exam exam;
    private TestpressExamApiClient apiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        exam = getArguments().getParcelable(PARAM_EXAM);
        apiClient = new TestpressExamApiClient(getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);
        listView.setDividerHeight(0);
    }

    @Override
    protected AttemptsPager getPager() {
        if (pager == null) {
            String state = getArguments().getString(PARAM_STATE);
            pager = new AttemptsPager(exam, apiClient);
            if (state != null && state.equals(STATE_PAUSED)) {
                pager.setQueryParams(PARAM_STATE, STATE_PAUSED);
            }
        }
        return (AttemptsPager) pager;
    }

    @Override
    protected SingleTypeAdapter<Attempt> createAdapter(List<Attempt> items) {
        return new AttemptsListAdapter(getActivity(), items, exam, R.layout.testpress_attempts_list_item);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        if (exception.getCause() instanceof IOException) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_no_internet_try_again;
        } else {
            setEmptyText(R.string.testpress_error_loading_attempts,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_attempts, R.string.testpress_no_attempts_description,
                    R.drawable.ic_error_outline_black_18dp);
    }
}
