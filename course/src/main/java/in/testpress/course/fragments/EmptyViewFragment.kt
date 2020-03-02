package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressException
import `in`.testpress.course.R
import android.content.Context
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView


class EmptyViewFragment : Fragment() {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var emptyTitleView: TextView
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var emptyDescView: TextView
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var emptyContainer: LinearLayout
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var retryButton: Button

    private var emptyViewListener: EmptyViewListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.testpress_empty_view, container, false);
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initializeListeners()
    }

    private fun initializeListeners() {
        emptyViewListener = if (parentFragment != null) {
            parentFragment as? EmptyViewListener
        } else {
            context as? EmptyViewListener
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyContainer = view.findViewById(R.id.empty_container)
        emptyTitleView = view.findViewById(R.id.empty_title)
        emptyDescView = view.findViewById(R.id.empty_description)
        retryButton = view.findViewById(R.id.retry_button)
    }

    fun displayError(exception: TestpressException) {
        when {
            exception.isForbidden -> handleForbidden()
            exception.isNetworkError -> handleNetworkError()
            exception.isPageNotFound -> handleIsPageNotFound()
            else -> handleUnknownError()
        }
    }

    private fun handleForbidden() {
        setEmptyText(R.string.permission_denied,
                R.string.testpress_no_permission,
                R.drawable.ic_error_outline_black_18dp)
        retryButton.visibility = View.GONE
    }

    private fun handleNetworkError() {
        setEmptyText(R.string.testpress_network_error,
                R.string.testpress_no_internet_try_again,
                R.drawable.ic_error_outline_black_18dp)
        retryButton.setOnClickListener {
            emptyContainer.visibility = View.GONE
            emptyViewListener?.onRetryClick()
        }
    }

    private fun handleIsPageNotFound() {
        setEmptyText(R.string.testpress_content_not_available,
                R.string.testpress_content_not_available_description,
                R.drawable.ic_error_outline_black_18dp)
    }

    private fun handleUnknownError() {
        setEmptyText(R.string.testpress_error_loading_contents,
                R.string.testpress_some_thing_went_wrong_try_again,
                R.drawable.ic_error_outline_black_18dp)
    }

    private fun setEmptyText(title: Int, description: Int, left: Int) {
        emptyContainer.visibility = View.VISIBLE
        emptyTitleView.setText(title)
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0)
        emptyDescView.setText(description)
        retryButton.visibility = View.VISIBLE
    }
}

interface EmptyViewListener {
    fun onRetryClick()
}