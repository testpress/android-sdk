package `in`.testpress.course.ui

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.api.TestpressCourseApiClient
import `in`.testpress.course.models.Reputation
import `in`.testpress.exam.databinding.TestpressFragmentCarouselWithEmptyViewBinding
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
    private lateinit var binding : TestpressFragmentCarouselWithEmptyViewBinding

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
        binding = TestpressFragmentCarouselWithEmptyViewBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        initializeOnClickListener()
    }

    private fun bindViews(){
        carouselView = binding.fragmentCarousel.root
        viewPager = binding.fragmentCarousel.viewpager
        tabLayout = binding.fragmentCarousel.tabLayout
        emptyView = binding.fragmentEmptyView.root
        emptyViewImage = binding.fragmentEmptyView.image
        emptyTitleView = binding.fragmentEmptyView.emptyTitle
        emptyDescView = binding.fragmentEmptyView.emptyDescription
        emptyTitleView.typeface = TestpressSdk.getRubikMediumFont(requireContext())
        emptyDescView.typeface = TestpressSdk.getRubikRegularFont(requireContext())
        retryButton = binding.fragmentEmptyView.retryButton
        progressBar = binding.pbLoading
        UIUtils.setIndeterminateDrawable(activity, progressBar, 4)
        carouselView.visibility = View.GONE
    }

    private fun initializeOnClickListener(){
        retryButton.setOnClickListener {
            emptyView.visibility = View.GONE
            loadMyReputation()
        }
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