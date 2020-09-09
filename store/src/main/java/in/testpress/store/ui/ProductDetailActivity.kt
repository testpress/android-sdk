package `in`.testpress.store.ui

import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.database.Course
import `in`.testpress.database.ProductDetailEntity
import `in`.testpress.enums.Status
import `in`.testpress.models.InstituteSettings
import `in`.testpress.store.R
import `in`.testpress.store.TestpressStore
import `in`.testpress.store.models.Product
import `in`.testpress.store.repository.ProductDetailRepository
import `in`.testpress.store.viewmodel.ProductDetailViewModel
import `in`.testpress.ui.BaseToolBarActivity
import `in`.testpress.util.*
import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_product_detail.*
import kotlinx.android.synthetic.main.product_detail_content_layout.*
import kotlinx.android.synthetic.main.testpress_product_details_layout.*

class ProductDetailActivity : BaseToolBarActivity() {

    private lateinit var productSlug: String
    private lateinit var emptyView: LinearLayout
    private lateinit var emptyTitleView: TextView
    private lateinit var emptyDescription: TextView
    private lateinit var retryButton: Button
    private lateinit var eventsTrackerFacade: EventsTrackerFacade
    private lateinit var viewModel: ProductDetailViewModel
    private lateinit var product: ProductDetailEntity
    private var coursesList: List<Course?>? = null
    val session: TestpressSession? = TestpressSdk.getTestpressSession(this)

    companion object {
        const val PRODUCT_SLUG = "productSlug"
        const val PRODUCT = "product"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)
        getDataFromBundle()
        initializeViews()
        initViewModel()
        setClickListeners()
    }

    private fun getDataFromBundle() {
        productSlug = intent?.getStringExtra(PRODUCT_SLUG) ?:
                      (intent.getParcelableExtra<Parcelable>(PRODUCT) as Product).slug
    }

    private fun initializeViews() {
        emptyView = findViewById(R.id.empty_container)
        emptyTitleView = findViewById(R.id.empty_title)
        emptyDescription = findViewById(R.id.empty_description)
        retryButton = findViewById(R.id.retry_button)
        UIUtils.setIndeterminateDrawable(this, progressbar, 4)
        eventsTrackerFacade = EventsTrackerFacade(applicationContext)
        buyButton.typeface = TestpressSdk.getRubikMediumFont(this)
        productTitle.typeface = TestpressSdk.getRubikMediumFont(this)

    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return ProductDetailViewModel(
                        ProductDetailRepository(this@ProductDetailActivity)
                ) as T
            }
        }).get(ProductDetailViewModel::class.java)
    }

    private fun loadProductDetails() {
        viewModel.loadProductsList(isInternetConnected(), productSlug).observe(this, Observer { resource ->
            when (resource.status) {
                Status.SUCCESS -> handleSuccessResponse(resource.data)
                Status.ERROR -> resource.exception?.let { handleException(it) }
            }
        })
    }

    private fun isInternetConnected(): Boolean {
        return InternetConnectivityChecker.isConnected(this)
    }

    private fun handleSuccessResponse(response: ProductDetailEntity?) {
        response?.let {
            setProductDetailsView(it)
        } ?: setEmptyProduct()
    }

    private fun setProductDetailsView(product: ProductDetailEntity) {
        progressbar.visibility = View.GONE
        productDetailView.visibility = View.VISIBLE
        productTitle.text = product.title
        buyButton.text = product.buyNowText
        currentPrice.text = product.currentPrice
        setProductThumbnail()
        setActualPrice()
        setExamsCount()
        setContentsCount()
        setDescriptionVisibility()
    }

    private fun setProductThumbnail() {
        val imageLoader = ImageUtils.initImageLoader(this)
        val options = ImageUtils.getPlaceholdersOption()
        imageLoader.displayImage(product.image, thumbnailImage, options)
    }

    private fun setActualPrice() {
        actualPrice.visibility = View.VISIBLE
        actualPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        actualPrice.text = product.prices?.get(0)?.price ?: ""
    }

    private fun setExamsCount() {
        if (coursesList?.isNotEmpty() == true) {
            var examsCount = 0
            product.courses?.forEach {
                examsCount += it?.examsCount ?: 0
            }
            totalExams.text = "$examsCount Exams"
            setHaveAccessCode()
        }
    }

    private fun setHaveAccessCode() {
        val settings: InstituteSettings? = session?.instituteSettings
        if (settings?.isAccessCodeEnabled == true) {
            haveAccessCode.visibility = View.VISIBLE
        } else {
            haveAccessCode.visibility = View.GONE
        }
    }

    private fun setContentsCount() {
        if (product.courses?.isNotEmpty() == true) {
            var attachmentCount: Int = 0
            product.courses?.forEach {
                attachmentCount += it?.attachmentsCount ?: 0
            }
            totalDocs.text = "$attachmentCount docs"
        }
    }

    private fun setDescriptionVisibility() {
        if (product.descriptionHtml?.isEmpty() == true) {
            description.visibility = View.GONE
        } else {
            description.visibility = View.VISIBLE
            setDescriptionFromHtml()
        }
    }

    private fun setDescriptionFromHtml() {
        val html = Html.fromHtml(product.descriptionHtml,
                UILImageGetter(description, this), null)
        description.setText(
                ZoomableImageString.convertString(html, this, false),
                TextView.BufferType.SPANNABLE)
        description.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setEmptyProduct() {
        setEmptyText(R.string.testpress_no_product_found,
                R.string.testpress_no_product_found_description,
                R.drawable.ic_error_outline_black_18dp)
        retryButton.visibility = View.INVISIBLE
    }

    private fun handleException(exception: TestpressException) {
        when {
            exception.isUnauthenticated -> {
                setEmptyText(R.string.testpress_authentication_failed,
                        R.string.testpress_no_permission,
                        R.drawable.ic_error_outline_black_18dp)
                retryButton.visibility = View.GONE
            }
            exception.isClientError -> {
                setEmptyText(R.string.testpress_no_product_found,
                        R.string.testpress_no_product_found_description,
                        R.drawable.ic_error_outline_black_18dp)
                retryButton.visibility = View.INVISIBLE
            }
            exception.isNetworkError -> {
                setEmptyText(R.string.testpress_network_error,
                        R.string.testpress_no_internet_try_again,
                        R.drawable.ic_error_outline_black_18dp)
            }
            else -> {
                setEmptyText(R.string.error_loading_products,
                        R.string.testpress_some_thing_went_wrong_try_again,
                        R.drawable.ic_error_outline_black_18dp)
                retryButton.visibility = View.GONE
            }
        }
    }

    private fun setEmptyText(title: Int, description: Int, left: Int) {
        emptyView.visibility = View.VISIBLE
        emptyTitleView.setText(title)
        emptyDescription.setText(description)
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0)
        retryButton.visibility = View.VISIBLE
    }

    private fun setClickListeners() {
        retryButton.setOnClickListener {
            loadProductDetails()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == TestpressStore.STORE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }
}
