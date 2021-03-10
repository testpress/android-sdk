package in.testpress.course.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.models.InstituteSettings;

class LeaderboardTabAdapter extends FragmentPagerAdapter {
    private InstituteSettings instituteSettings;
    private Bundle bundle;

    LeaderboardTabAdapter(Context context, FragmentManager fragmentManager, Bundle bundle) {
        super(fragmentManager);
        this.bundle = bundle;
        instituteSettings = TestpressSdk.getTestpressSession(context).getInstituteSettings();
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
                return instituteSettings.getLeaderboardLabel();
            case 1:
                return instituteSettings.getThreatsAndTargetsLabel();
            default:
                return null;
        }
    }

}
