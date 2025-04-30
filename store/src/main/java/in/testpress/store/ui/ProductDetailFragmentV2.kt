package `in`.testpress.store.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import `in`.testpress.store.databinding.TestpressProductDetailsFragmentV2Binding
import `in`.testpress.util.ImageUtils

class ProductDetailFragmentV2 : Fragment() {
    private var _binding: TestpressProductDetailsFragmentV2Binding? = null
    private val binding get() = _binding!!

    private val tabTitles = listOf("Overview", "Curriculum")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TestpressProductDetailsFragmentV2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUrl = "https://d36vpug2b5drql.cloudfront.net/institute/lmsdemo/product_images/330/73bb56e30eb4451a927fcded0e43ffdd.jpeg"
        ImageUtils.initImageLoader(requireContext()).displayImage(
            imageUrl, binding.productThumbnail, ImageUtils.getPlaceholdersOption()
        )

        val pagerAdapter = ProductDetailsPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun show(activity: FragmentActivity, containerViewId: Int) {
            activity.supportFragmentManager.beginTransaction()
                .replace(containerViewId, ProductDetailFragmentV2())
                .commit()
        }
    }

    private inner class ProductDetailsPagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = tabTitles.size

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ProductOverviewFragment()
                1 -> ProductCurriculumFragment()
                else -> throw IllegalArgumentException("Invalid tab index")
            }
        }
    }
}

class ProductOverviewFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val textView = TextView(requireContext())
        textView.text = "Product Overview"
        textView.textSize = 18f
        return NestedScrollView(requireContext()).apply {
            addView(textView)
        }
    }
}

class ProductCurriculumFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val textView = TextView(requireContext())
        textView.text = "Curriculum Content"
        textView.textSize = 18f
        return NestedScrollView(requireContext()).apply {
            addView(textView)
        }
    }
}

