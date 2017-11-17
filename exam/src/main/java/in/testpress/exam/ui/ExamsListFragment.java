package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.exam.R;
import in.testpress.models.greendao.Exam;
import in.testpress.exam.network.ExamPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.ui.PagedItemFragment;
import in.testpress.util.SingleTypeAdapter;

public class ExamsListFragment extends PagedItemFragment<Exam> {

    private String subclass;
    private String category;
    private TestpressExamApiClient apiClient;
    public static final String AVAILABLE = "available";
    public static final String UPCOMING = "upcoming";
    public static final String HISTORY = "history";
    public static final String SUBCLASS = "subclass";
    public static final String CATEGORY = "category";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        subclass = getArguments().getString(SUBCLASS);
        category = getArguments().getString(CATEGORY);
        apiClient = new TestpressExamApiClient(getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.testpress_search, menu);
        MenuItem searchMenu = menu.findItem(R.id.search);
        Drawable searchIcon = searchMenu.getIcon();
        searchIcon.mutate().setColorFilter(ContextCompat.getColor(getActivity(),
                R.color.testpress_actionbar_text), PorterDuff.Mode.SRC_IN);
        searchMenu.setIcon(searchIcon);
    }

    @Override
    protected ExamPager getPager() {
        if (pager == null) {
            pager = new ExamPager(subclass, category, apiClient);
        }
        return (ExamPager)pager;
    }

    @Override
    protected SingleTypeAdapter<Exam> createAdapter(List<Exam> items) {
        if (subclass != null) {
            if (subclass.equals(ExamsListFragment.UPCOMING)) {
                return new UpcomingExamsListAdapter(getActivity(), items);
            } else if (subclass.equals(ExamsListFragment.HISTORY)) {
                return new HistoryListAdapter(getParentFragment(), items);
            }
        }
        return new AvailableExamsListAdapter(getParentFragment(), items);
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
        if (subclass.equals("history")) {
            setEmptyText(R.string.testpress_no_attempts, R.string.testpress_no_attempts_description,
                    R.drawable.ic_error_outline_black_18dp);
        } else {
            setEmptyText(R.string.testpress_no_exams, R.string.testpress_no_exams_description,
                    R.drawable.ic_error_outline_black_18dp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.search == item.getItemId()) {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            intent.putExtra(SearchFragment.SUBCLASS, subclass);
            intent.putExtra(SearchFragment.CATEGORY, category);
            getParentFragment().startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
