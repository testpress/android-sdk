package `in`.testpress.store.ui

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import `in`.testpress.database.entities.DomainProduct
import `in`.testpress.enums.Status
import `in`.testpress.store.R
import `in`.testpress.store.data.repository.ProductDetailRepository
import `in`.testpress.store.databinding.TestpressProductDetailsDescriptionFragmentBinding
import `in`.testpress.store.databinding.TestpressProductDetailsFragmentV2Binding
import `in`.testpress.store.ui.viewmodel.ProductViewModel
import `in`.testpress.util.ImageUtils
import `in`.testpress.util.UILImageGetter
import `in`.testpress.util.ZoomableImageString

class ProductDetailFragmentV2 : Fragment() {
    private var _binding: TestpressProductDetailsFragmentV2Binding? = null
    private val binding get() = _binding!!
    private var productId: Int = DEFAULT_PRODUCT_ID
    private var domainProduct: DomainProduct? = null

    private val tabTitles = listOf("Description", "Course Curriculum")
    private val tabIcons = listOf(
        R.drawable.baseline_description_24,
        R.drawable.baseline_menu_book_24
    )

    private val productViewModel: ProductViewModel by lazy {
        ViewModelProvider(requireActivity(), ProductViewModelFactory(requireContext(), productId))
            .get(ProductViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = requireArguments().getInt(ProductDetailsActivityV2.PRODUCT_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TestpressProductDetailsFragmentV2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeProduct()
        val pagerAdapter = ProductDetailsPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
            tab.setIcon(tabIcons[position])
        }.attach()
    }

    private fun observeProduct() {
        productViewModel.product.observe(viewLifecycleOwner) { resource ->
            when (resource?.status) {
                Status.LOADING -> {}
                Status.SUCCESS -> {
                    this.domainProduct = resource.data
                    renderProductDetails()
                }
                Status.ERROR -> {}
                else -> Unit
            }
        }
    }

    private fun renderProductDetails() {
        val imageUrl = domainProduct?.product?.images?.get(0)?.original
        ImageUtils.initImageLoader(requireContext()).displayImage(
            imageUrl, binding.productThumbnail, ImageUtils.getPlaceholdersOption()
        )
        binding.title.text = domainProduct?.product?.title

        binding.buttonContainer.couponEditText.addTextChangedListener(afterTextChanged = {
            binding.buttonContainer.applyCouponButton.isEnabled = it?.trim()?.isNotEmpty() == true
        })

        binding.buttonContainer.showCouponButton.apply {
            setOnClickListener {
                if (binding.buttonContainer.couponContainer.visibility == View.GONE) {
                    binding.buttonContainer.couponContainer.visibility = View.VISIBLE
                } else {
                    binding.buttonContainer.couponContainer.visibility = View.GONE
                }
            }
        }

        binding.buttonContainer.applyCouponButton.setOnClickListener {
            val enteredCoupon = binding.buttonContainer.couponEditText.text.toString()
            Toast.makeText(requireContext(),"Apply Button Clicked",Toast.LENGTH_SHORT).show()
        }

        binding.buttonContainer.priceText.text = String.format("₹%s", domainProduct?.product?.price)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val DEFAULT_PRODUCT_ID = -1
        fun show(activity: FragmentActivity, containerViewId: Int, productId: Int) {
            val fragment = ProductDetailFragmentV2().apply {
                arguments = Bundle().apply {
                    putInt(ProductDetailsActivityV2.PRODUCT_ID, productId)
                }
            }
            activity.supportFragmentManager.beginTransaction()
                .replace(containerViewId, fragment)
                .commit()
        }
    }

    private inner class ProductDetailsPagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = tabTitles.size

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ProductDescriptionFragment()
                1 -> ProductCurriculumFragment()
                else -> throw IllegalArgumentException("Invalid tab index")
            }
        }
    }
}

class ProductViewModelFactory(
    private val context: Context,
    private val productId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProductViewModel(ProductDetailRepository(context, productId)) as T
    }
}

class ProductDescriptionFragment : Fragment() {

    private var _binding: TestpressProductDetailsDescriptionFragmentBinding? = null
    private val binding get() = _binding!!
    private var domainProduct: DomainProduct? = null

    private val productViewModel: ProductViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            TestpressProductDetailsDescriptionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeProduct()
    }

    private fun observeProduct() {
        productViewModel.product.observe(viewLifecycleOwner) { resource ->
            when (resource?.status) {
                Status.LOADING -> {}
                Status.SUCCESS -> {
                    this.domainProduct = resource.data
                    renderDescription()
                }
                Status.ERROR -> {}
                else -> Unit
            }
        }
    }

    private fun renderDescription() {
        domainProduct?.let { domainProduct ->
            val product = domainProduct.product
            val hasDescription = !product.descriptionHtml.isNullOrEmpty()

            if (hasDescription) {
                val html = Html.fromHtml(
                    product.descriptionHtml,
                    UILImageGetter(binding.description, requireActivity()),
                    null
                )

                binding.description.apply {
                    setText(
                        ZoomableImageString.convertString(html, requireActivity(), false),
                        TextView.BufferType.SPANNABLE
                    )
                    movementMethod = LinkMovementMethod.getInstance()
                    isVisible = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ProductCurriculumFragment : Fragment() {

    private lateinit var adapter: NodeAdapter
    private lateinit var courseData: Course
    private lateinit var displayItems: List<DisplayItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_product_curriculum, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        courseData = getDummyCourse() // Replace with real API call
        displayItems = flattenChapters(courseData.chapters)

        adapter = NodeAdapter(courseData) { updatedList ->
            displayItems = updatedList
            adapter.updateData(displayItems)
        }

        adapter.updateData(displayItems)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        return view
    }

    private fun getDummyCourse(): Course {
        val content1 = Content("c1", "Video 1")
        val content2 = Content("c2", "PDF 1")
        val content3 = Content("c3", "Exam 1")

        val level3Chapter = Chapter("ch3", "Chapter 3", contentList = listOf(content1, content2, content3))
        val level2Chapter = Chapter("ch2", "Chapter 2", children = listOf(level3Chapter))

        // The top-level chapter (level 1) contains the rest
        val level1Chapter = Chapter("ch1", "Chapter 1", children = listOf(level2Chapter))

        // Now add some content to the root chapter (level 1)
        val rootChapter = Chapter("ch0", "Root Chapter", children = listOf(level1Chapter))

        return Course("course1", "Sample Course", listOf(rootChapter))
    }

    class NodeAdapter(
        private val course: Course,
        private val onDataUpdated: (List<DisplayItem>) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val TYPE_CHAPTER = 0
            private const val TYPE_CONTENT = 1
        }

        private var items: List<DisplayItem> = emptyList()

        override fun getItemViewType(position: Int): Int {
            return when (items[position]) {
                is DisplayItem.ChapterItem -> TYPE_CHAPTER
                is DisplayItem.ContentItem -> TYPE_CONTENT
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                TYPE_CHAPTER -> ChapterViewHolder(inflater.inflate(R.layout.item_chapter, parent, false))
                TYPE_CONTENT -> ContentViewHolder(inflater.inflate(R.layout.item_content, parent, false))
                else -> throw IllegalArgumentException("Invalid view type")
            }
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is DisplayItem.ChapterItem -> (holder as ChapterViewHolder).bind(item.chapter)
                is DisplayItem.ContentItem -> (holder as ContentViewHolder).bind(item.content, item.level)
            }
        }

        fun updateData(newItems: List<DisplayItem>) {
            this.items = newItems
            notifyDataSetChanged()
        }

        inner class ChapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val titleText = view.findViewById<TextView>(R.id.titleText)
            private val indentView = view.findViewById<View>(R.id.indentView)
            private val arrowIcon = view.findViewById<ImageView>(R.id.arrowIcon)

            fun bind(chapter: Chapter) {
                titleText.text = chapter.title
                indentView.layoutParams.width = chapter.level * 40
                indentView.requestLayout()

                arrowIcon.setImageResource(
                    if (chapter.isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
                )

                itemView.setOnClickListener {
                    chapter.isExpanded = !chapter.isExpanded
                    val updatedItems = flattenChapters(course.chapters)
                    onDataUpdated(updatedItems)
                }
            }

            private fun flattenChapters(chapters: List<Chapter>, level: Int = 0): List<DisplayItem> {
                val result = mutableListOf<DisplayItem>()
                for (chapter in chapters) {
                    chapter.level = level
                    result.add(DisplayItem.ChapterItem(chapter))
                    if (chapter.isExpanded) {
                        result.addAll(flattenChapters(chapter.children, level + 1))
                        chapter.contentList.forEach {
                            result.add(DisplayItem.ContentItem(it, level + 1))
                        }
                    }
                }
                return result
            }
        }

        inner class ContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val titleText = view.findViewById<TextView>(R.id.titleText)
            private val indentView = view.findViewById<View>(R.id.indentView)

            fun bind(content: Content, level: Int) {
                titleText.text = content.title
                indentView.layoutParams.width = level * 40
                indentView.requestLayout()
            }
        }
    }

    private fun flattenChapters(chapters: List<Chapter>, level: Int = 0): List<DisplayItem> {
        val result = mutableListOf<DisplayItem>()
        for (chapter in chapters) {
            chapter.level = level
            result.add(DisplayItem.ChapterItem(chapter))
            if (chapter.isExpanded) {
                result.addAll(flattenChapters(chapter.children, level + 1))
                chapter.contentList.forEach {
                    result.add(DisplayItem.ContentItem(it, level + 1))
                }
            }
        }
        return result
    }

    sealed class DisplayItem {
        data class ChapterItem(val chapter: Chapter) : DisplayItem()
        data class ContentItem(val content: Content, val level: Int) : DisplayItem()
    }

    data class Content(val id: String, val title: String)

    data class Chapter(
        val id: String,
        val title: String,
        val children: List<Chapter> = emptyList(),
        val contentList: List<Content> = emptyList(),
        var isExpanded: Boolean = false,
        var level: Int = 0
    )

    data class Course(
        val id: String,
        val title: String,
        val chapters: List<Chapter> = emptyList()
    )
}

class ProductSubjectsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val textView = TextView(requireContext())
        textView.text = "Subjects"
        textView.textSize = 18f
        return NestedScrollView(requireContext()).apply {
            addView(textView)
        }
    }
}

class ProductFAQFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val textView = TextView(requireContext())
        textView.text = "FAQ"
        textView.textSize = 18f
        return NestedScrollView(requireContext()).apply {
            addView(textView)
        }
    }
}

