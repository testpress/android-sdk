package in.testpress.course.ui;

import static in.testpress.course.ui.ChapterDetailActivity.TITLE;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

class CourseDetailsTabAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragmentList;

    CourseDetailsTabAdapter(
            FragmentManager fragmentManager,
            ArrayList<Fragment> fragmentList
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
        return fragmentList.get(position);
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        return fragmentList.get(position).getArguments().getString(TITLE);
    }

}
