package `in`.testpress.store.ui

import `in`.testpress.core.TestpressException
import `in`.testpress.database.ProductsItem
import `in`.testpress.network.BaseResourcePager
import `in`.testpress.store.R
import `in`.testpress.store.TestpressStore
import `in`.testpress.store.models.Product
import `in`.testpress.store.models.ProductsList
import `in`.testpress.store.network.ProductsPager
import `in`.testpress.store.network.TestpressStoreApiClient
import `in`.testpress.ui.PagedItemFragment
import `in`.testpress.util.SingleTypeAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class ProductsListFragment: Fragment() {

    private var apiClient: TestpressStoreApiClient? = null

    companion object {
        fun show(activity: FragmentActivity, containerViewId: Int) {
            activity.supportFragmentManager.beginTransaction()
                    .replace(containerViewId, ProductListFragment())
                    .commitAllowingStateLoss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_products_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    private fun initRecyclerView() {

    }
}