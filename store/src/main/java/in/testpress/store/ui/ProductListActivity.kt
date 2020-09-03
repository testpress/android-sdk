package `in`.testpress.store.ui

import `in`.testpress.core.TestpressException
import `in`.testpress.enums.Status
import `in`.testpress.store.R
import `in`.testpress.store.repository.ProductsListRepository
import `in`.testpress.store.viewmodel.ProductsListViewModel
import `in`.testpress.ui.BaseToolBarActivity
import `in`.testpress.util.InternetConnectivityChecker
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_products_list.*

class ProductListActivity: BaseToolBarActivity() {

    private lateinit var productsListViewModel: ProductsListViewModel
    private var productsListAdapter = ProductListAdapter()
    private lateinit var retryButton: Button
    private lateinit var emptyContainer: LinearLayout
    private lateinit var emptyTitle: TextView
    private lateinit var emptyDescription: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products_list)
        setActionBarTitle()
        initializeViews()
        initViewModel()
        getDataFromViewModel()
        initRecyclerView()
        setListeners()
    }

    private fun setActionBarTitle() {
        if (intent.getStringExtra("title") != "" && intent.getStringExtra("title") != null) {
            supportActionBar!!.title = intent.getStringExtra("title")
        }
    }

    private fun initializeViews() {
        retryButton = findViewById(R.id.retry_button)
        emptyContainer = findViewById(R.id.empty_container)
        emptyDescription = findViewById(R.id.empty_description)
        emptyTitle = findViewById(R.id.empty_title)
    }

    private fun initViewModel() {
        productsListViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return ProductsListViewModel(
                            ProductsListRepository(this@ProductListActivity)
                    ) as T
            }
        }).get(ProductsListViewModel::class.java)
    }

    private fun getDataFromViewModel() {
        productsListViewModel.loadProductsList(isInternetConnected()).observe(this, Observer { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    productsListAdapter.setData(resource.data?.courses, resource.data?.products)
                    productsListAdapter.notifyDataSetChanged()
                    progressbar.visibility = View.GONE
                }
                Status.ERROR -> {
                    progressbar.visibility = View.GONE
                    resource.exception?.let {
                        handleException(it)
                    } ?: run {
                        setEmptyText(R.string.error_loading_products,
                                R.string.testpress_some_thing_went_wrong_try_again)
                        retryButton.visibility = View.GONE
                    }
                }
                Status.LOADING -> progressbar.visibility = View.VISIBLE
            }
        })
    }

    private fun isInternetConnected(): Boolean {
        return InternetConnectivityChecker.isConnected(this)
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.adapter = productsListAdapter
    }

    private fun handleException(exception: TestpressException) {
        when {
            exception.isUnauthenticated -> {
                setEmptyText(R.string.testpress_authentication_failed,
                        R.string.testpress_no_permission)
                retryButton.visibility = View.GONE
            }
            exception.isNetworkError -> {
                setEmptyText(R.string.testpress_network_error,
                        R.string.testpress_no_internet_try_again)
            }
            else -> {
                setEmptyText(R.string.error_loading_products,
                        R.string.testpress_some_thing_went_wrong_try_again)
                retryButton.visibility = View.GONE
            }
        }
    }

    private fun setEmptyText(title: Int, description: Int) {
        emptyContainer.visibility = View.VISIBLE
        emptyTitle.setText(title)
        emptyDescription.setText(description)
        retryButton.visibility = View.VISIBLE
    }

    private fun setListeners() {
        retryButton.setOnClickListener {
            getDataFromViewModel()
        }

        swipeToRefresh.setOnRefreshListener {
            getDataFromViewModel()
            swipeToRefresh.isRefreshing = false
        }
    }

}
