package in.testpress.exam.ui;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import in.testpress.exam.R;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.Exam;

class ReviewPagerAdapter extends FragmentPagerAdapter {

    private final Resources resources;
    private Exam exam;
    private Attempt attempt;

    ReviewPagerAdapter(Fragment fragment, Exam exam, Attempt attempt) {
        super(fragment.getChildFragmentManager());
        this.exam = exam;
        this.attempt = attempt;
        resources = fragment.getResources();
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public Fragment getItem(final int position) {
        final Fragment result;
        String filter = "";
        switch (position) {
            case 1:
                filter = "all";
                break;
            case 2:
                filter = "correct";
                break;
            case 3:
                filter = "incorrect";
                break;
            case 4:
                filter = "unanswered";
                break;
            case 5:
                filter = "review";
                break;
            case 0:
            default:
                filter = "stats";
                break;
        }
        if (filter.equals("stats")) {
            result = ReviewStatsFragment.getInstance(exam, attempt);
        } else {
            result = ReviewQuestionsFragment.getInstance(attempt, filter);
        }
        return result;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.testpress_page_stats);
            case 1:
                return resources.getString(R.string.testpress_page_all);
            case 2:
                return resources.getString(R.string.testpress_page_correct);
            case 3:
                return resources.getString(R.string.testpress_page_incorrect);
            case 4:
                return resources.getString(R.string.testpress_page_unanswered);
            case 5:
                return resources.getString(R.string.testpress_page_review);
            default:
                return null;
        }
    }
}
