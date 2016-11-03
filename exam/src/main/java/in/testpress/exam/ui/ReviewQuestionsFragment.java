package in.testpress.exam.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.exam.R;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.ReviewItem;
import in.testpress.exam.network.ReviewQuestionsPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.ui.PagedItemFragment;
import in.testpress.util.SingleTypeAdapter;

public class ReviewQuestionsFragment extends PagedItemFragment<ReviewItem> {

    public static final String PARAM_ATTEMPT = "attempt";
    public static final String PARAM_FILTER = "filter";
    private Attempt attempt;
    private String filter;

    public static ReviewQuestionsFragment getInstance(Attempt attempt, String filter) {
        ReviewQuestionsFragment fragment = new ReviewQuestionsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_FILTER, filter);
        bundle.putParcelable(PARAM_ATTEMPT, attempt);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        attempt = getArguments().getParcelable(PARAM_ATTEMPT);
        filter = getArguments().getString(PARAM_FILTER);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);
        listView.setDividerHeight(0);
    }

    @Override
    protected ReviewQuestionsPager getPager() {
        if (pager == null) {
            pager = new ReviewQuestionsPager(attempt.getReviewFrag(), filter,
                    new TestpressExamApiClient(getActivity()));
        }
        return (ReviewQuestionsPager) pager;
    }

    @Override
    protected SingleTypeAdapter<ReviewItem> createAdapter(List<ReviewItem> items) {
        return new ReviewListAdapter(R.layout.testpress_review_question,
                getActivity().getLayoutInflater(), items, getActivity());
    }

    @Override
    protected int getErrorMessage(TestpressException exception) {
        if (exception.isUnauthenticated()) {
            setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_authentication_failed;
        } else  if (exception.isNetworkError()) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_no_internet_try_again;
        } else {
            setEmptyText(R.string.testpress_error_loading_questions,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_questions, R.string.testpress_no_questions_to_review,
                R.drawable.ic_error_outline_black_18dp);
    }
}