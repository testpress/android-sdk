package in.testpress.exam.ui;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import in.testpress.exam.R;

public class ExamPagerAdapter extends FragmentPagerAdapter {
    private final Resources resources;
    private Bundle bundle;

    /**
     * Create pager adapter
     *
     * @param resources
     * @param fragmentManager
     */
    public ExamPagerAdapter(final Resources resources, final FragmentManager fragmentManager,
                            Bundle bundle) {
        super(fragmentManager);
        this.resources = resources;
        this.bundle = bundle;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(int position) {
        String subclass;
        switch (position) {
            case 0:
                subclass = ExamsListFragment.AVAILABLE;
                break;
            case 1:
                subclass = ExamsListFragment.UPCOMING;
                break;
            case 2:
                subclass = ExamsListFragment.HISTORY;
                break;
            default:
                subclass = null;
                break;
        }
        ExamsListFragment fragment = new ExamsListFragment();
        Bundle bundle;
        if (this.bundle == null) {
            bundle = new Bundle();
        } else {
            bundle = new Bundle(this.bundle);
        }
        if (subclass != null) {
            bundle.putString(ExamsListFragment.SUBCLASS, subclass);
            fragment.setArguments(bundle);
        }
        return fragment;

    }

    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.testpress_page_available_exams);
            case 1:
                return resources.getString(R.string.testpress_page_upcoming_exams);
            case 2:
                return resources.getString(R.string.testpress_page_history_exams);
            default:
                return null;
        }
    }

}
