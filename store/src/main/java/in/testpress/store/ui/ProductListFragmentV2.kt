package `in`.testpress.store.ui

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
import `in`.testpress.database.entities.ProductLiteEntity
import `in`.testpress.enums.Status
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.store.R
import `in`.testpress.store.databinding.TestpressProductListFragmentBinding
import `in`.testpress.store.ui.adapter.FooterState
import `in`.testpress.store.ui.adapter.ProductListAdapter
import `in`.testpress.store.ui.viewmodel.ProductListViewModel

class ProductListFragmentV2 : Fragment(), EmptyViewListener {
    private var _binding: TestpressProductListFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var viewModel: ProductListViewModel
    private lateinit var adapter: ProductListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ProductListViewModel.init(requireActivity());
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
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.empty_view_container, emptyViewFragment)
            .commit()
    }

    private fun setupRecyclerView() {
        adapter = ProductListAdapter(requireContext()) { viewModel.retryNextPage() }
        val layoutManager = LinearLayoutManager(requireContext())
        binding.productList.apply {
            this.adapter = adapter
            this.layoutManager = layoutManager
            this.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        addPaginationScrollListener(layoutManager)
    }

    private fun addPaginationScrollListener(layoutManager: LinearLayoutManager) {
        binding.productList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && isAtEndOfList(layoutManager)) {
                    viewModel.fetchNextPage()
                }
            }
        })
    }

    private fun isAtEndOfList(layoutManager: LinearLayoutManager): Boolean {
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        return (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0
    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { resource ->
            when (resource?.status) {
                Status.LOADING -> handleLoadingState()
                Status.SUCCESS -> handleSuccessState(resource.data)
                Status.ERROR -> handleErrorState(resource.exception)
                else -> { /* No-op */ }
            }
        }
    }

    private fun handleLoadingState() {
        binding.emptyViewContainer.isVisible = false
        if (adapter.itemCount > 0) {
            adapter.updateFooterState(FooterState.LOADING)
        } else {
            binding.shimmerViewContainer.isVisible = true
        }
    }

    private fun handleSuccessState(data: List<ProductLiteEntity>?) {
        adapter.submitList(data)
        adapter.updateFooterState(FooterState.HIDDEN)
        if (adapter.itemCount == 0) {
            binding.emptyViewContainer.isVisible = true
            displayEmptyScreen()
        } else {
            binding.productList.isVisible = true
            binding.emptyViewContainer.isVisible = false
        }
        binding.shimmerViewContainer.isVisible = false
    }

    private fun handleErrorState(exception: TestpressException?) {
        binding.shimmerViewContainer.isVisible = false
        if (adapter.itemCount > 0) {
            binding.productList.isVisible = true
            adapter.updateFooterState(FooterState.ERROR)
        } else {
            binding.productList.isVisible = false
            binding.emptyViewContainer.isVisible = true
            val errorToDisplay = exception ?: TestpressException.unexpectedWebViewError(UnknownError())
            emptyViewFragment.displayError(errorToDisplay)
        }
    }

    private fun displayEmptyScreen() {
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
        viewModel.retryNextPage()
    }

    companion object {
        fun show(activity: FragmentActivity, containerViewId: Int) {
            activity.supportFragmentManager.beginTransaction()
                .replace(containerViewId, ProductListFragmentV2())
                .commitAllowingStateLoss()
        }
    }
}
