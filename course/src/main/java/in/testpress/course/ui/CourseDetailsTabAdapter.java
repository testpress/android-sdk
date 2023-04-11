package in.testpress.course.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.LinkedHashMap;

class CourseDetailsTabAdapter extends FragmentPagerAdapter {

    private LinkedHashMap<Fragment, String> fragmentList;

    CourseDetailsTabAdapter(
            FragmentManager fragmentManager,
            LinkedHashMap<Fragment, String> fragmentList
    ) {
        super(fragmentManager);
        this.fragmentList = fragmentList;
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public Fragment getItem(int position) {
        return (Fragment) fragmentList.keySet().toArray()[position];
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        return fragmentList.get(getItem(position));
    }

}
