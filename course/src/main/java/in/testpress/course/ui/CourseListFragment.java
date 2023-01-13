package in.testpress.course.ui;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.R;
import in.testpress.store.TestpressStore;
import in.testpress.store.ui.ProductListFragment;
import in.testpress.ui.BaseFragment;

public class CourseListFragment extends BaseFragment {
    private TabLayout tabs;
    private Adapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.testpress_fragment_carousel,container, false);

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabs = (TabLayout) view.findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(viewPager);

        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new MyCoursesFragment(), getString(R.string.my_course_title));

        TestpressSession session = TestpressSdk.getTestpressSession(getContext());
        String storeLabel = "Available Courses";
        if (session.getInstituteSettings().getStoreLabel() != null && !session.getInstituteSettings().getStoreLabel().isEmpty()) {
            storeLabel = session.getInstituteSettings().getStoreLabel();
        }
        adapter.addFragment(new AvailableCourseListFragment(), storeLabel);
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }


        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TestpressStore.STORE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && !data.getBooleanExtra(TestpressStore.CONTINUE_PURCHASE, false)) {
            tabs.getTabAt(0).select();
            MyCoursesFragment fragment = (MyCoursesFragment) adapter.getItem(0);
            fragment.clearItemsAndRefresh();
        }
    }
}
