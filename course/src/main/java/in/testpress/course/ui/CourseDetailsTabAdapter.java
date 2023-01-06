package in.testpress.course.ui;

import android.content.res.Resources;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import in.testpress.course.R;
import in.testpress.course.fragments.RunningContentsListFragment;

class CourseDetailsTabAdapter extends FragmentPagerAdapter {

    private Resources resources;
    private Bundle bundle;

    CourseDetailsTabAdapter(Resources resources, FragmentManager fragmentManager, Bundle bundle) {
        super(fragmentManager);
        this.resources = resources;
        this.bundle = bundle;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new ChaptersListFragment();
                break;
            case 1:
                fragment = new RankListFragment();
                break;
            case 2:
                fragment = new RunningContentsListFragment();
                break;
            case 3:
                fragment = new RunningContentsListFragment();
                break;
            case 4:
                fragment = new RunningContentsListFragment();
                break;
            default:
                fragment = new ChaptersListFragment();
                break;
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.testpress_learn);
            case 1:
                return resources.getString(R.string.testpress_leaderboard);
            case 2:
                return "Running";
            case 3:
                return "Upcoming";
            case 4:
                return "History";
            default:
                return null;
        }
    }

}
