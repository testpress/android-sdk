package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.exam.R;
import in.testpress.exam.models.Exam;
import in.testpress.exam.network.ExamPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.ui.PagedItemFragment;
import in.testpress.util.SingleTypeAdapter;

import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;

public class AccessCodeExamsFragment extends PagedItemFragment<Exam> {

    public static final String EXAMS = "exams";
    public static final String ACCESS_CODE = "access_code";

    private String accessCode;
    private TestpressExamApiClient apiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        accessCode = getArguments().getString(ACCESS_CODE);
        apiClient = new TestpressExamApiClient(getActivity());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.testpress_exams);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected ExamPager getPager() {
        if (pager == null) {
            pager = new ExamPager(accessCode, apiClient);
        }
        return (ExamPager) pager;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        List<Exam> exams = getArguments().getParcelableArrayList(EXAMS);
        if (exams != null && !exams.isEmpty()) {
            firstCallBack = false;
            pager = getPager();
            pager.setResources(exams);
            pager.page = 2;
            updateItems(exams);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected SingleTypeAdapter<Exam> createAdapter(List<Exam> items) {
        return new HistoryListAdapter(this, items, accessCode);
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
            setEmptyText(R.string.testpress_error_loading_exams, R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_exams, R.string.testpress_no_exams_description,
                    R.drawable.ic_error_outline_black_18dp);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == TEST_TAKEN_REQUEST_CODE) && (Activity.RESULT_OK == resultCode)) {
            clearItemsAndRefresh();
        }
    }

}
