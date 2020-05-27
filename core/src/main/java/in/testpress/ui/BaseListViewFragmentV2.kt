package `in`.testpress.ui

import `in`.testpress.R
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.util.SingleTypeAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout


abstract class BaseListViewFragmentV2<E, T> : Fragment() {
}