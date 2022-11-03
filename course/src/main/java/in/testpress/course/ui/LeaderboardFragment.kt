package `in`.testpress.course.ui

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.api.TestpressCourseApiClient
import `in`.testpress.course.models.Reputation
import `in`.testpress.network.RetrofitCall
import `in`.testpress.ui.BaseFragment
import `in`.testpress.util.UIUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import java.io.IOException

class LeaderboardFragment : BaseFragment() {

    private lateinit var carouselView: LinearLayout
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var emptyView: LinearLayout
    private lateinit var emptyViewImage: ImageView
    private lateinit var emptyTitleView: TextView
    private lateinit var emptyDescView: TextView
    private lateinit var retryButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var myReputationApiRequest: RetrofitCall<Reputation>

    companion object {
        fun show(activity: FragmentActivity, containerViewId: Int) {
            activity.supportFragmentManager.beginTransaction()
                .replace(containerViewId, LeaderboardFragment())
                .commitAllowingStateLoss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(
            R.layout.testpress_fragment_carousel_with_empty_view, container, false
        )

        carouselView = view.findViewById<LinearLayout>(R.id.fragment_carousel) as LinearLayout
        viewPager = view.findViewById<ViewPager>(R.id.viewpager) as ViewPager
        tabLayout = view.findViewById<TabLayout>(R.id.tab_layout) as TabLayout
        emptyView = view.findViewById<LinearLayout>(R.id.empty_container) as LinearLayout
        emptyViewImage = view.findViewById<ImageView>(R.id.image_view) as ImageView
        emptyTitleView = view.findViewById<TextView>(R.id.empty_title) as TextView
        emptyDescView = view.findViewById<TextView>(R.id.empty_description) as TextView
        emptyTitleView.typeface = TestpressSdk.getRubikMediumFont(requireContext())
        emptyDescView.typeface = TestpressSdk.getRubikRegularFont(requireContext())
        retryButton = view.findViewById<Button>(R.id.retry_button) as Button
        progressBar = view.findViewById<ProgressBar>(R.id.pb_loading) as ProgressBar
        UIUtils.setIndeterminateDrawable(activity, progressBar, 4)
        carouselView.visibility = View.GONE
        retryButton.setOnClickListener {
            emptyView.visibility = View.GONE
            loadMyReputation()
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadMyReputation()
    }

    private fun loadMyReputation() {
        progressBar.visibility = View.VISIBLE
        myReputationApiRequest = TestpressCourseApiClient(context).myRank
            .enqueue(object : TestpressCallback<Reputation>() {
                override fun onSuccess(reputation: Reputation) {
                    if (activity == null) {
                        return
                    }
                    val bundle = Bundle()
                    bundle.putParcelable(RankListFragment.PARAM_USER_REPUTATION, reputation)
                    val adapter = LeaderboardTabAdapter(
                        context!!,
                        childFragmentManager, bundle
                    )
                    viewPager.adapter = adapter
                    tabLayout.setupWithViewPager(viewPager)
                    progressBar.visibility = View.GONE
                    carouselView.visibility = View.VISIBLE
                }

                override fun onException(exception: TestpressException) {
                    if (exception.isNetworkError) {
                        setEmptyText(
                            R.string.testpress_network_error,
                            R.string.testpress_no_internet_try_again,
                            R.drawable.testpress_no_wifi
                        )
                    } else if (exception.cause is IOException) {
                        setEmptyText(
                            R.string.testpress_authentication_failed,
                            R.string.testpress_please_login,
                            R.drawable.testpress_alert_warning
                        )
                        retryButton.visibility = View.INVISIBLE
                    } else {
                        setEmptyText(
                            R.string.testpress_error_loading_rank,
                            R.string.testpress_some_thing_went_wrong_try_again,
                            R.drawable.testpress_alert_warning
                        )
                        retryButton.visibility = View.INVISIBLE
                    }
                }
            })
    }

    private fun setEmptyText(title: Int, description: Int, imageRes: Int) {
        progressBar.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        emptyTitleView.setText(title)
        emptyViewImage.setImageResource(imageRes)
        emptyDescView.setText(description)
    }

    override fun getRetrofitCalls(): Array<RetrofitCall<*>> {
        return arrayOf(myReputationApiRequest)
    }
}