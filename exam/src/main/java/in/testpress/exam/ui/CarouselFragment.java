package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.testpress.exam.R;

public class CarouselFragment extends Fragment {

    public static final int TEST_TAKEN_REQUEST_CODE = 9999;
    public static final String CURRENT_ITEM = "currentItem";
    private ExamPagerAdapter adapter;
    private ViewPager viewPager;

    public static void show(FragmentActivity activity, int containerViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new CarouselFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.testpress_fragment_carousel, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        adapter = new ExamPagerAdapter(getResources(), getChildFragmentManager(), getArguments());
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
        Bundle data = getArguments();
        if (data != null) {
            viewPager.setCurrentItem(data.getInt(CarouselFragment.CURRENT_ITEM, 0));
        } else {
            viewPager.setCurrentItem(0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == TEST_TAKEN_REQUEST_CODE) && (Activity.RESULT_OK == resultCode)) {
            if ((viewPager.getCurrentItem() == 0)) {
                // If current tab is available exams then refresh it & move to history tab
                if (getFragmentByPosition(0) != null) {
                    getFragmentByPosition(0).refreshWithProgress();
                }
                viewPager.setCurrentItem(2);
            }
            if (getFragmentByPosition(2) != null && getFragmentByPosition(2).getListView() != null) {
                // Refresh history tab
                getFragmentByPosition(2).clearItemsAndRefresh();
            }
        }
    }

    public ExamsListFragment getFragmentByPosition(int position) {
        return (ExamsListFragment) getChildFragmentManager().findFragmentByTag("android:switcher:"
                + viewPager.getId() + ":" + adapter.getItemId(position));
    }
}
