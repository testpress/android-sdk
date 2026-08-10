package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.models.SSOUrl
import `in`.testpress.network.TestpressApiClient
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class AssignmentFragment : BaseContentDetailFragment() {

    override var isBookmarkEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.assignment_content_layout, container, false)
    }

    override fun display() {
        val session = TestpressSdk.getTestpressSession(requireContext())
        val baseUrl = session?.instituteSettings?.baseUrl
        if (baseUrl.isNullOrBlank()) {
            emptyViewFragment.displayError(TestpressException.unexpectedError(Exception("Base URL cannot be null or empty")))
            return
        }

        val courseId = content.courseId
        if (courseId == null) {
            emptyViewFragment.displayError(TestpressException.unexpectedError(Exception("Course ID cannot be null")))
            return
        }

        val assignmentUrl = "$baseUrl/courses/$courseId/contents/${content.id}/"

        TestpressApiClient(requireContext(), session).ssourl
            .enqueue(object : TestpressCallback<SSOUrl>() {
                override fun onSuccess(result: SSOUrl?) {
                    if (isAdded) {
                        val ssoUrl = baseUrl + result?.ssoUrl + "&next=" + assignmentUrl
                        val webViewFragment = WebViewFragment()
                        webViewFragment.arguments = Bundle().apply {
                            putString(WebViewFragment.URL_TO_OPEN, ssoUrl)
                            putBoolean(WebViewFragment.IS_AUTHENTICATION_REQUIRED, false)
                            putBoolean(WebViewFragment.ENABLE_SWIPE_REFRESH, true)
                            putBoolean(WebViewFragment.SHOW_LOADING_BETWEEN_PAGES, true)
                            putInt(WebViewFragment.CACHE_MODE, android.webkit.WebSettings.LOAD_NO_CACHE)
                        }
                        childFragmentManager.beginTransaction()
                            .replace(R.id.webview_container, webViewFragment)
                            .commitAllowingStateLoss()
                    }
                }

                override fun onException(exception: TestpressException?) {
                    if (isAdded) {
                        emptyViewFragment.displayError(exception!!)
                    }
                }
            })
    }
}
