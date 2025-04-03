package `in`.testpress.store.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import `in`.testpress.R
import `in`.testpress.core.TestpressException
import `in`.testpress.enums.Status
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
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
        initializeEmptyViewFragment()
        setupRecyclerView()
        observeViewModel()
    }

    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.empty_view_container, emptyViewFragment)
            .commit()
    }

    private fun setupRecyclerView() {
        adapter = ProductListAdapter(requireContext()) { viewModel.retryNextPage() }
        val layoutManager = LinearLayoutManager(requireContext())
        binding.productList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        binding.productList.layoutManager = layoutManager
        binding.productList.adapter = adapter

        binding.productList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { // Check for scroll down
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        viewModel.fetchNextPage()
                    }
                }
            }
        })
    }


    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { resource->
            when (resource?.status) {
                Status.LOADING -> {
                    binding.emptyViewContainer.isVisible = false
                    if (adapter.itemCount > 0){
                        adapter.updateFooterState(FooterState.LOADING)
                    } else {
                        binding.shimmerViewContainer.isVisible = true
                    }
                }
                Status.SUCCESS -> {
                    binding.productList.isVisible = true
                    binding.emptyViewContainer.isVisible = false
                    binding.shimmerViewContainer.isVisible = false
                    adapter.submitList(resource.data)
                    adapter.updateFooterState(FooterState.HIDDEN)
                }
                Status.ERROR -> {
                    binding.shimmerViewContainer.isVisible = false
                    if (adapter.itemCount > 0) {
                        binding.productList.isVisible = true
                        adapter.updateFooterState(FooterState.ERROR)
                    } else {
                        binding.productList.isVisible = false
                        binding.emptyViewContainer.isVisible = true
                        emptyViewFragment.displayError(resource.exception!!)
                        resource.exception?.let {
                            emptyViewFragment.displayError(it)
                        } ?: emptyViewFragment.displayError(
                            TestpressException.unexpectedWebViewError(UnknownError())
                        )
                    }
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRetryClick() {
        viewModel.retryNextPage()
    }
}
