package in.testpress.course.ui;

import static in.testpress.fragments.WebViewFragment.ALLOW_NON_INSTITUTE_URL_IN_WEB_VIEW;
import static in.testpress.fragments.WebViewFragment.ENABLE_SWIPE_REFRESH;
import static in.testpress.fragments.WebViewFragment.IS_AUTHENTICATION_REQUIRED;
import static in.testpress.fragments.WebViewFragment.SHOW_LOADING_BETWEEN_PAGES;
import static in.testpress.fragments.WebViewFragment.URL_TO_OPEN;

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
import in.testpress.fragments.WebViewFragment;
import in.testpress.store.TestpressStore;
import in.testpress.ui.BaseFragment;
import in.testpress.util.CommonUtils;

public class CourseListFragment extends BaseFragment {
    private TabLayout tabs;
    private Adapter adapter;
    private WebViewFragment webViewFragment;
    private boolean isWebViewVisibleToUser = false;
    private TestpressSession session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        session = TestpressSdk.getTestpressSession(getContext());
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

        String storeLabel = "Available Courses";
        if (session.getInstituteSettings().getStoreLabel() != null && !session.getInstituteSettings().getStoreLabel().isEmpty()) {
            storeLabel = session.getInstituteSettings().getStoreLabel();
        }
        addStoreFragment(storeLabel);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                isWebViewVisibleToUser = (position == 1);
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void addStoreFragment(String storeLabel) {
        if (isStoreDisabled()) {
            return;
        }
        // Here we are adding Custom store WebView for EPratibha App
        if (isEPratibhaApp()) {
            addEPratibhaWebViewFragment(storeLabel);
        } else {
            adapter.addFragment(new AvailableCourseListFragment(), storeLabel);
        }
    }

    private void addEPratibhaWebViewFragment(String storeLabel) {
        String[] credentials = CommonUtils.getUserCredentials(requireContext());
        WebViewFragment webViewFragment = new WebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(URL_TO_OPEN, "https://www.epratibha.net/mobile-login/?email=" + credentials[0] + "&pass=" + credentials[1]);
        bundle.putBoolean(SHOW_LOADING_BETWEEN_PAGES, true);
        bundle.putBoolean(IS_AUTHENTICATION_REQUIRED, false);
        bundle.putBoolean(ALLOW_NON_INSTITUTE_URL_IN_WEB_VIEW, true);
        bundle.putBoolean(ENABLE_SWIPE_REFRESH, true);
        webViewFragment.setArguments(bundle);
        adapter.addFragment(webViewFragment, storeLabel);
    }

    private boolean isStoreDisabled() {
        return session.getInstituteSettings() != null
                && (!session.getInstituteSettings().getStoreEnabled()
                || session.getInstituteSettings().getDisableStoreInApp());
    }

    private boolean isEPratibhaApp() {
        return requireContext().getPackageName().equals("net.epratibha.www");
    }

    public Boolean onBackPress(){
        if (webViewFragment != null && isWebViewVisibleToUser && webViewFragment.canGoBack()){
            webViewFragment.goBack();
            return false;
        } else {
            return true;
        }
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
