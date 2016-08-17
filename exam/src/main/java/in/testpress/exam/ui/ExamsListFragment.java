package in.testpress.exam.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import java.io.IOException;
import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.Exam;
import in.testpress.exam.network.ExamPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.util.SingleTypeAdapter;

public class ExamsListFragment extends PagedItemFragment<Exam> {

    private String subclass;
    private TestpressExamApiClient apiClient;
    public static final String AVAILABLE = "available";
    public static final String UPCOMING = "upcoming";
    public static final String HISTORY = "history";
    public static final String SUBCLASS = "subclass";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        subclass = getArguments().getString(ExamsListFragment.SUBCLASS);
        apiClient = new TestpressExamApiClient();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);
        listView.setDividerHeight(0);
    }

    @Override
    protected ExamPager getPager() {
        if (pager == null) {
            pager = new ExamPager(subclass, apiClient);
        }
        return (ExamPager)pager;
    }

    @Override
    protected SingleTypeAdapter<Exam> createAdapter(List<Exam> items) {
        if (subclass != null) {
            if (subclass.equals(ExamsListFragment.UPCOMING)) {
                return new UpcomingExamsListAdapter(getActivity(), items,
                        R.layout.upcoming_exams_list_item);
            } else if (subclass.equals(ExamsListFragment.HISTORY)) {
                return new HistoryListAdapter(getActivity(), items, R.layout.history_exams_list_item);
            }
        }
        return new AvailableExamsListAdapter(getActivity(), items, R.layout.available_exams_list_item);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        if((exception.getMessage() != null) && (exception.getMessage()).equals("403 FORBIDDEN")) {
            setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_authentication_failed;
        } else if (exception.getCause() instanceof IOException) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_no_internet_try_again;
        } else {
            setEmptyText(R.string.testpress_error_loading_exams, R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        if (subclass.equals("history")) {
            setEmptyText(R.string.testpress_no_attempts, R.string.testpress_no_attempts_description,
                    R.drawable.ic_error_outline_black_18dp);
        } else {
            setEmptyText(R.string.testpress_no_exams, R.string.testpress_no_exams_description,
                    R.drawable.ic_error_outline_black_18dp);
        }
    }

}
