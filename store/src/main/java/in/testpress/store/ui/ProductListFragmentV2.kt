package `in`.testpress.store.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import `in`.testpress.core.TestpressException
import `in`.testpress.database.entities.ProductCategoryEntity
import `in`.testpress.database.entities.ProductLiteEntity
import `in`.testpress.enums.Status
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.store.R
import `in`.testpress.store.TestpressStore
import `in`.testpress.store.databinding.TestpressProductListFragmentBinding
import `in`.testpress.store.ui.adapter.FooterState
import `in`.testpress.store.ui.adapter.ProductCategoryAdapter
import `in`.testpress.store.ui.adapter.ProductListAdapter
import `in`.testpress.store.ui.viewmodel.ProductCategoryViewModel
import `in`.testpress.store.ui.viewmodel.ProductListViewModel


class ProductListFragmentV2 : Fragment(), EmptyViewListener {
    private var _binding: TestpressProductListFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var productsViewModel: ProductListViewModel
    private lateinit var categoriesViewModel: ProductCategoryViewModel
    private lateinit var productsAdapter: ProductListAdapter
    private lateinit var categoriesAdapter: ProductCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productsViewModel = ProductListViewModel.init(requireActivity())
        categoriesViewModel = ProductCategoryViewModel.init(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TestpressProductListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEmptyViewFragment()
        setupCategoryList()
        setupProductList()
        observeViewModels()
    }

    private fun setupEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.empty_view_container, emptyViewFragment)
            .commit()
    }

    private fun setupCategoryList() {
        categoriesAdapter = ProductCategoryAdapter(
            onRetry = { categoriesViewModel.retryNextPage() },
            onCategorySelected = {/* TODO: Add Filter option on category selection */ }
        )

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.productCategoriesList.apply {
            adapter = categoriesAdapter
            this.layoutManager = layoutManager
            addOnScrollListener(getPaginationScrollListener(layoutManager) {
                categoriesViewModel.fetchNextPage()
            })
        }
    }


    private fun setupProductList() {
        productsAdapter = ProductListAdapter(
            requireContext(),
            onRetry = { productsViewModel.retryNextPage() },
            onItemClick = { product ->
                val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
                intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, product.slug)
                activity!!.startActivityForResult(intent, TestpressStore.STORE_REQUEST_CODE)
            }
        )

        val layoutManager = LinearLayoutManager(requireContext())
        binding.productList.apply {
            adapter = productsAdapter
            this.layoutManager = layoutManager
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            addOnScrollListener(getPaginationScrollListener(layoutManager) {
                productsViewModel.fetchNextPage()
            })
        }
    }

    private fun getPaginationScrollListener(
        layoutManager: LinearLayoutManager,
        onEndReached: () -> Unit
    ) = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val scrollForward = dx > 0 || dy > 0
            if (scrollForward && isEndOfListReached(layoutManager)) {
                onEndReached()
            }
        }
    }

    private fun isEndOfListReached(layoutManager: LinearLayoutManager): Boolean {
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        return (visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                firstVisibleItemPosition >= 0
    }

    private fun observeViewModels() {
        observeCategories()
        observeProducts()
    }

    private fun observeCategories() {
        categoriesViewModel.categories.observe(viewLifecycleOwner) { resource ->
            when (resource?.status) {
                Status.LOADING -> showCategoryLoading(resource.data)
                Status.SUCCESS -> showCategorySuccess(resource.data)
                Status.ERROR -> showCategoryError()
                else -> Unit
            }
        }
    }

    private fun observeProducts() {
        productsViewModel.products.observe(viewLifecycleOwner) { resource ->
            when (resource?.status) {
                Status.LOADING -> showProductLoading(resource.data)
                Status.SUCCESS -> showProductSuccess(resource.data)
                Status.ERROR -> showProductError(resource.exception)
                else -> Unit
            }
        }
    }

    private fun showCategoryLoading(categories: List<ProductCategoryEntity>?) {
        binding.productCategoriesLayout.isVisible = true
        categoriesAdapter.submitList(categories)
        if (categoriesAdapter.itemCount > 0) {
            categoriesAdapter.updateFooterState(FooterState.LOADING)
        } else {
            binding.productCategoryShimmerViewContainer.isVisible = true
        }
    }

    private fun showCategorySuccess(categories: List<ProductCategoryEntity>?) {
        binding.productCategoriesLayout.isVisible = true
        categoriesAdapter.submitList(categories)
        categoriesAdapter.updateFooterState(FooterState.HIDDEN)
        binding.productCategoriesList.isVisible = categoriesAdapter.itemCount > 0
        binding.productCategoryShimmerViewContainer.isVisible = false
    }

    private fun showCategoryError() {
        binding.productCategoryShimmerViewContainer.isVisible = false
        if (categoriesAdapter.itemCount > 0) {
            binding.productCategoriesList.isVisible = true
            categoriesAdapter.updateFooterState(FooterState.ERROR)
        } else {
            binding.productCategoriesLayout.isVisible = false
        }
    }

    private fun showProductLoading(products: List<ProductLiteEntity>?) {
        binding.productsLayout.isVisible = true
        productsAdapter.submitList(products)
        binding.emptyViewContainer.isVisible = false
        if (productsAdapter.itemCount > 0) {
            productsAdapter.updateFooterState(FooterState.LOADING)
        } else {
            binding.shimmerViewContainer.isVisible = true
        }
    }

    private fun showProductSuccess(products: List<ProductLiteEntity>?) {
        binding.productsLayout.isVisible = true
        productsAdapter.submitList(products)
        productsAdapter.updateFooterState(FooterState.HIDDEN)

        binding.shimmerViewContainer.isVisible = false
        binding.emptyViewContainer.isVisible = products.isNullOrEmpty()

        if (products.isNullOrEmpty()) {
            showEmptyState()
            binding.productsLayout.isVisible = false
        } else {
            binding.productList.isVisible = true
        }
    }

    private fun showProductError(exception: TestpressException?) {
        binding.shimmerViewContainer.isVisible = false

        if (productsAdapter.itemCount > 0) {
            binding.productList.isVisible = true
            productsAdapter.updateFooterState(FooterState.ERROR)
        } else {
            binding.productsLayout.isVisible = false
            binding.emptyViewContainer.isVisible = true
            emptyViewFragment.displayError(
                exception ?: TestpressException.unexpectedWebViewError(UnknownError())
            )
        }
    }

    private fun showEmptyState() {
        emptyViewFragment.apply {
            setEmptyText(R.string.no_course_title, R.string.no_course_description, null)
            setImage(R.drawable.empty_cart)
            showOrHideButton(false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRetryClick() {
        productsViewModel.retryNextPage()
        categoriesViewModel.retryNextPage()
    }

    companion object {
        fun show(activity: FragmentActivity, containerViewId: Int) {
            activity.supportFragmentManager.beginTransaction()
                .replace(containerViewId, ProductListFragmentV2())
                .commitAllowingStateLoss()
        }
    }
}
