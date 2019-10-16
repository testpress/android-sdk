package in.testpress.course.ui;

import android.content.res.Resources;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import in.testpress.course.R;

class LeaderboardTabAdapter extends FragmentPagerAdapter {
    private final Resources resources;
    private Bundle bundle;

    LeaderboardTabAdapter(Resources resources, FragmentManager fragmentManager, Bundle bundle) {
        super(fragmentManager);
        this.resources = resources;
        this.bundle = bundle;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new RankListFragment();
                break;
            case 1:
                fragment = new TargetThreadFragment();
                break;
            default:
                fragment = new RankListFragment();
                break;
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.testpress_leaderboard);
            case 1:
                return resources.getString(R.string.testpress_targets_or_threads);
            default:
                return null;
        }
    }

}
