package `in`.testpress.fragments

import `in`.testpress.R
import `in`.testpress.core.TestpressException
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import org.json.JSONException
import org.json.JSONObject

class EmptyViewFragment : Fragment() {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var emptyTitleView: TextView
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var emptyDescView: TextView
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var emptyContainer: LinearLayout
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var retryButton: Button
    private lateinit var image: ImageView

    private var emptyViewListener: EmptyViewListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.testpress_empty_view, container, false)
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
        bindViews(view)
        retryButton.setOnClickListener {
            emptyContainer.visibility = View.GONE
            emptyViewListener?.onRetryClick()
        }
    }

    private fun bindViews(view: View) {
        emptyContainer = view.findViewById(R.id.empty_container)
        emptyTitleView = view.findViewById(R.id.empty_title)
        emptyDescView = view.findViewById(R.id.empty_description)
        retryButton = view.findViewById(R.id.retry_button)
        image = view.findViewById(R.id.image)
    }

    fun displayError(exception: TestpressException) {
        when {
            exception.isForbidden -> handleForbidden(exception)
            exception.isNetworkError -> handleNetworkError()
            exception.isPageNotFound -> handleIsPageNotFound()
            exception.isWebViewUnexpectedError -> handleWebViewUnknownError(exception)
            else -> handleUnknownError()
        }
    }

    private fun handleForbidden(exception: TestpressException) {
        val errorResponse = exception.response?.errorBody()?.string()?.let {
            try {
                JSONObject(it)
            } catch (e: JSONException) {
                null
            }
        }

        if (isScheduledContent(errorResponse)) {
            showScheduledContentMessage(errorResponse?.getString("message")!!)
        } else if (errorResponse?.has("detail") == true) {
            showCustomPermissionDeniedMessage(errorResponse.getString("detail"))
        } else {
            showPermissionDeniedMessage()
        }
    }

    private fun isScheduledContent(errorResponse: JSONObject?) =
        errorResponse?.has("error_code") == true && errorResponse.getString("error_code")
            .equals("scheduled")

    private fun showScheduledContentMessage(message: String){
        setEmptyText(
            R.string.content_scheduled,
            message,
            R.drawable.ic_error_outline_black_18dp
        )
    }

    private fun showCustomPermissionDeniedMessage(message: String){
        setEmptyText(
            R.string.permission_denied,
            message,
            R.drawable.ic_error_outline_black_18dp
        )
    }

    private fun showPermissionDeniedMessage(){
        setEmptyText(
            R.string.permission_denied,
            R.string.testpress_no_permission,
            R.drawable.ic_error_outline_black_18dp
        )
    }

    private fun handleNetworkError() {
        setEmptyText(R.string.testpress_network_error,
                R.string.testpress_no_internet_try_again,
                R.drawable.ic_error_outline_black_18dp)
    }

    private fun handleIsPageNotFound() {
        setEmptyText(R.string.testpress_content_not_available,
                R.string.testpress_content_not_available_description,
                R.drawable.ic_error_outline_black_18dp)
    }

    private fun handleWebViewUnknownError(exception: TestpressException) {
        val message = exception.message ?: "Unknown WebView Error"
        setEmptyText(R.string.testpress_error_loading_contents,
            message,
            R.drawable.ic_error_outline_black_18dp)
    }

    private fun handleUnknownError() {
        setEmptyText(R.string.testpress_error_loading_contents,
                R.string.testpress_some_thing_went_wrong_try_again,
                R.drawable.ic_error_outline_black_18dp)
    }

    fun showViewsExhaustedMessage(){
        setEmptyText(
            R.string.video_id_locked,
            R.string.testpress_views_exhausted,
            R.drawable.ic_error_outline_black_18dp
        )
        retryButton.isVisible = false
    }

    fun setEmptyText(title: Int, description : Int, leftDrawable: Int?) {
        emptyContainer.visibility = View.VISIBLE
        emptyTitleView.setText(title)
        if (leftDrawable != null) {
            emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, 0, 0, 0)
        }
        emptyDescView.setText(description)
        retryButton.visibility = View.VISIBLE
    }

    fun setEmptyText(title: Int, description : String, leftDrawable: Int?) {
        emptyContainer.visibility = View.VISIBLE
        emptyTitleView.setText(title)
        if (leftDrawable != null) {
            emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, 0, 0, 0)
        }
        emptyDescView.setText(description)
        retryButton.visibility = View.VISIBLE
    }

    fun setImage(resId: Int) {
        image.setImageResource(resId)
    }

    fun showOrHideButton(show: Boolean) {
        if (show) {
            retryButton.visibility = View.VISIBLE
        } else {
            retryButton.visibility = View.GONE
        }
    }

    fun hide() {
        emptyContainer.visibility = View.GONE
    }
}

interface EmptyViewListener {
    fun onRetryClick()
}
