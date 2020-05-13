package `in`.testpress.ui

import `in`.testpress.R
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.util.SingleTypeAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout

abstract class BaseListViewFragmentV2<E>: Fragment() {
    protected lateinit var listView: ListView
    protected lateinit var loadingPlaceholder: ShimmerFrameLayout
    protected lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var emptyViewFragment: EmptyViewFragment
    protected var items: List<E> = emptyList()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.base_list_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        swipeRefreshLayout.setOnRefreshListener {
            refreshWithProgress()
        }
        listView.adapter = createAdapter()
        if (isItemsEmpty()) {
            swipeRefreshLayout.isRefreshing = true
        }
        initializeEmptyViewFragment()
    }

    fun bindViews(view: View) {
        listView = view.findViewById(android.R.id.list)
        loadingPlaceholder = view.findViewById(R.id.shimmer_view_container)
        swipeRefreshLayout = view.findViewById(R.id.swipe_container)
        swipeRefreshLayout.setColorSchemeResources(R.color.testpress_color_primary)
    }


    protected fun createAdapter(): HeaderFooterListAdapter<SingleTypeAdapter<E>> {
        val wrapped: SingleTypeAdapter<E> = createAdapter(items)
        return HeaderFooterListAdapter<SingleTypeAdapter<E>>(
            listView,
            wrapped
        )
    }

    fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.empty_view_fragment, emptyViewFragment)
        transaction.commit()
    }

    protected open fun getListAdapter(): HeaderFooterListAdapter<SingleTypeAdapter<E>> {
        return listView.adapter as HeaderFooterListAdapter<SingleTypeAdapter<E>>
    }

    protected abstract fun createAdapter(items: List<E>): SingleTypeAdapter<E>

    protected abstract fun refreshWithProgress()

    protected abstract fun isItemsEmpty(): Boolean
}