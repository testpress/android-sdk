package `in`.testpress.store.ui

import `in`.testpress.database.CoursesItem
import `in`.testpress.database.ProductsItem
import `in`.testpress.enums.Status
import `in`.testpress.store.R
import `in`.testpress.store.TestpressStore
import `in`.testpress.store.repository.ProductsListRepository
import `in`.testpress.store.viewmodel.ProductsListViewModel
import `in`.testpress.ui.BaseToolBarActivity
import `in`.testpress.util.ViewUtils.handleException
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_products_list.*

class ProductListActivity: BaseToolBarActivity() {

    private lateinit var productsListViewModel: ProductsListViewModel
    private var productsListAdapter = ProductListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_products_list)
        setActionBarTitle()
        initViewModel()
        getDataFromViewModel()
        initRecyclerView()
    }

    private fun setActionBarTitle() {
        if (intent.getStringExtra("title") != "" && intent.getStringExtra("title") != null) {
            supportActionBar!!.title = intent.getStringExtra("title")
        }
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
        productsListViewModel.loadProductsList().observe(this, Observer { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                productsListAdapter.setData(resource.data?.courses, resource.data?.products)
                productsListAdapter.notifyDataSetChanged()
                }
                Status.ERROR -> {
                    handleException(resource.exception, rootLayout,R.string.testpress_no_products)
                }
            }
        })
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.adapter = productsListAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == TestpressStore.STORE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && !data.getBooleanExtra(TestpressStore.CONTINUE_PURCHASE, false)) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }
}
