package `in`.testpress.store.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import `in`.testpress.database.entities.DomainProduct
import `in`.testpress.enums.Status
import `in`.testpress.store.R
import `in`.testpress.store.data.repository.ProductDetailRepository
import `in`.testpress.store.databinding.BottomSheetApplyCouponBinding
import `in`.testpress.store.databinding.DialogProgressBinding
import `in`.testpress.store.databinding.TestpressProductDetailsDescriptionFragmentBinding
import `in`.testpress.store.databinding.TestpressProductDetailsFragmentV2Binding
import `in`.testpress.store.ui.viewmodel.ProductViewModel
import `in`.testpress.util.ImageUtils
import `in`.testpress.util.UILImageGetter
import `in`.testpress.util.ZoomableImageString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProductDetailFragmentV2 : Fragment() {
    private var _binding: TestpressProductDetailsFragmentV2Binding? = null
    private val binding get() = _binding!!
    private var productId: Int = DEFAULT_PRODUCT_ID
    private var domainProduct: DomainProduct? = null

    private val tabTitles = listOf("Description", "Course Curriculum")

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

        productViewModel.isCouponApplied.observe(viewLifecycleOwner) {
            domainProduct?.product?.let { product ->
                product.price?.let { price ->
                    displayFormattedPrice(it, price, product.strikeThroughPrice)
                }
            }
        }
    }

    private fun renderProductDetails() {
        val imageUrl = domainProduct?.product?.images?.get(0)?.original
        ImageUtils.initImageLoader(requireContext()).displayImage(
            imageUrl, binding.productThumbnail, ImageUtils.getPlaceholdersOption()
        )
        binding.title.text = domainProduct?.product?.title

        binding.buttonContainer.showCouponButton.apply {
            setOnClickListener {
                val bottomSheet = ApplyCouponBottomSheet()
                bottomSheet.show(parentFragmentManager, ApplyCouponBottomSheet.TAG)
            }
        }

        domainProduct?.product?.let { product ->
            product.price?.let { price ->
                displayFormattedPrice(false, price,product.strikeThroughPrice)
            }
        }
    }

    private fun displayFormattedPrice(isCouponApplied: Boolean, currentPrice: String, strikePrice: String?) {

        val spanFlag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        val builder = SpannableStringBuilder()

        // Current price span
        builder.append(
            SpannableString("₹$currentPrice").apply {
                setSpan(StyleSpan(Typeface.BOLD), 0, length, spanFlag)
                setSpan(RelativeSizeSpan(1.2f), 0, length, spanFlag)
                setSpan(ForegroundColorSpan(Color.BLACK), 0, length, spanFlag)
            }
        )

        // Optional strike price span
        if (!strikePrice.isNullOrBlank()) {
            builder.append("  ") // spacing
            builder.append(
                SpannableString("₹$strikePrice").apply {
                    setSpan(StrikethroughSpan(), 0, length, spanFlag)
                    setSpan(RelativeSizeSpan(0.8f), 0, length, spanFlag)
                    setSpan(ForegroundColorSpan(Color.GRAY), 0, length, spanFlag)
                }
            )
        }

        if (isCouponApplied){
            builder.append("\n")
            builder.append(
                SpannableString("+${productViewModel.amountSaved} Saved").apply {
                    setSpan(RelativeSizeSpan(0.8f), 0, length, spanFlag)
                    setSpan(ForegroundColorSpan(Color.parseColor("#059669")), 0, length, spanFlag)
                }
            )
        }

        binding.buttonContainer.priceText.text = builder
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

class ApplyCouponBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetApplyCouponBinding? = null
    private val binding get() = _binding!!

    private var _dialogBinding: DialogProgressBinding? = null
    private val dialogBinding get() = _dialogBinding!!

    private var loadingDialog: AlertDialog? = null

    private val productViewModel: ProductViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetApplyCouponBinding.inflate(inflater, container, false)
        _dialogBinding = DialogProgressBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.couponEditText.addTextChangedListener(afterTextChanged = {
            binding.applyCouponButton.isEnabled = it?.trim()?.isNotEmpty() == true
            if (it.toString() != "" && it.toString() == productViewModel.currentCoupon){
                binding.applyCouponButton.text = "Remove"
                binding.applyCouponButton.setBackgroundResource(R.drawable.rounded_orange_button)
            } else {
                binding.applyCouponButton.text = "Apply"
                binding.applyCouponButton.setBackgroundResource(R.drawable.button_apply_coupon)
            }
        })

        binding.applyCouponButton.setOnClickListener {
            val isApplied = productViewModel.isCouponApplied.value == true

            hideKeyboard()
            showProgressDialog(if (isApplied) "Removing" else "Applying")

            lifecycleScope.launch {
                delay(2000) // Simulate network delay
                loadingDialog?.dismiss()

                if (isApplied) {
                    productViewModel.removeCoupon()
                    productViewModel.currentCoupon = ""
                    binding.couponAppliedText.isVisible = false
                    binding.couponEditText.setText("")
                    binding.applyCouponButton.text = "Apply"
                    binding.applyCouponButton.setBackgroundResource(R.drawable.button_apply_coupon)
                } else {
                    val couponText = binding.couponEditText.text.toString()
                    productViewModel.applyCoupon()
                    productViewModel.currentCoupon = couponText

                    val savedAmount = "%.2f".format(productViewModel.amountSaved)
                    binding.couponAppliedText.text = "$couponText Applied! You have saved ₹$savedAmount on this course."
                    binding.couponAppliedText.isVisible = true

                    binding.applyCouponButton.text = "Remove"
                    binding.applyCouponButton.setBackgroundResource(R.drawable.rounded_orange_button)
                }
            }
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        if (productViewModel.isCouponApplied.value == true){
            binding.couponEditText.setText(productViewModel.currentCoupon)
            binding.couponAppliedText.isVisible = true
            binding.couponAppliedText.text = "${productViewModel.currentCoupon} Applied! You have saved ₹${"%.2f".format(productViewModel.amountSaved)} on this course."

            binding.applyCouponButton.text = "Remove"
            binding.applyCouponButton.setBackgroundResource(R.drawable.rounded_orange_button)
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = dialog?.currentFocus ?: view ?: View(requireContext())
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showProgressDialog(message: String) {
        if (loadingDialog == null) {
            loadingDialog = MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.root)
                .setCancelable(false)
                .create()
        }
        dialogBinding.messageText.text = message
        loadingDialog?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ApplyCouponBottomSheet"
    }
}