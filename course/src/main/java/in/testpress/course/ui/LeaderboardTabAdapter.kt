package `in`.testpress.course.ui


import `in`.testpress.core.TestpressSdk
import `in`.testpress.models.InstituteSettings
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class LeaderboardTabAdapter(
    val context: Context,
    fragmentManager: FragmentManager,
    val bundle: Bundle
) : FragmentPagerAdapter(fragmentManager) {

    private var instituteSettings: InstituteSettings =
        TestpressSdk.getTestpressSession(context)?.instituteSettings!!

    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment {
        val fragment: Fragment = when (position) {
            0 -> RankListFragment()
            1 -> TargetThreadFragment()
            else -> RankListFragment()
        }
        fragment.arguments = bundle
        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> instituteSettings.leaderboardLabel
            1 -> instituteSettings.threatsAndTargetsLabel
            else -> null
        }
    }
}