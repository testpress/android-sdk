package `in`.testpress.course.ui;

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.ui.BaseFragment
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

class CourseListFragment: BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.testpress_fragment_carousel, container, false)

        val viewPager = view.findViewById<ViewPager>(R.id.viewpager)
        setupViewPager(viewPager)

        val tabsLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        tabsLayout.setupWithViewPager(viewPager)

        return view
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = Adapter(childFragmentManager)
        adapter.addFragment(MyCoursesFragment(), getString(R.string.my_course_title))

        val session = context?.let {
            TestpressSdk.getTestpressSession(it)
        }
        var storeLabel = "Available Courses"
        if (session?.instituteSettings?.storeLabel != null && !session.instituteSettings.storeLabel.isEmpty()) {
            storeLabel = session.instituteSettings.storeLabel
        }
        adapter.addFragment(AvailableCourseListFragment(), storeLabel)
        viewPager.adapter = adapter
    }

    @SuppressLint("WrongConstant")
    class Adapter(manager: FragmentManager) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val fragmentList: MutableList<Fragment> = arrayListOf()
        private val fragmentTitleList: MutableList<String> = arrayListOf()
        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }
    }


}
