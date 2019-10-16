package in.testpress.exam.ui;

import android.content.res.Resources;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import in.testpress.exam.R;

public class AnalyticsPagerAdapter extends FragmentPagerAdapter {

    private Resources resources;
    private Bundle bundle;

    public AnalyticsPagerAdapter(Resources resources, FragmentManager fragmentManager,
                                 Bundle bundle) {
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
                fragment = new StrengthAnalyticsGraphFragment();
                break;
            case 1:
                fragment = new IndividualSubjectAnalyticsFragment();
                break;
            default:
                fragment = new IndividualSubjectAnalyticsFragment();
                break;
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.testpress_overall_reports);
            case 1:
                return resources.getString(R.string.testpress_individual_reports);
            default:
                return null;
        }
    }

}
