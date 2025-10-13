package `in`.testpress.course.fragments

import `in`.testpress.course.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.fragments.WebViewFragment.Companion.IS_AUTHENTICATION_REQUIRED
import `in`.testpress.fragments.WebViewFragment.Companion.URL_TO_OPEN
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession


class AIChatPdfFragment : Fragment() {
    
    companion object {
        private const val ARG_CONTENT_ID = "contentId"
        private const val ARG_COURSE_ID = "courseId"
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.ai_pdf_fragment, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val contentId = requireArguments().getLong(ARG_CONTENT_ID, -1L)
        val courseId = requireArguments().getLong(ARG_COURSE_ID, -1L)
        
        if (contentId == -1L || courseId == -1L) {
            throw IllegalArgumentException("Required arguments (contentId, courseId) are missing or invalid.")
        }
        
        val webViewFragment = WebViewFragment()
        
        val pdfUrl = getPdfUrl(courseId, contentId)
    
        webViewFragment.arguments = Bundle().apply {
            putString(URL_TO_OPEN, pdfUrl)
            putBoolean(IS_AUTHENTICATION_REQUIRED, true)
        }
    
        childFragmentManager.beginTransaction()
            .replace(R.id.aiPdf_view_fragment, webViewFragment)
            .commit()
    }

    private fun getPdfUrl(courseId: Long, contentId: Long): String {
        val session = TestpressSdk.getTestpressSession(requireContext()) 
            ?: throw IllegalStateException("User session not found.")
        val baseUrl = session.instituteSettings?.domainUrl.takeIf { !it.isNullOrEmpty() }
            ?: throw IllegalStateException("Base URL not configured.")
        return "$baseUrl/courses/$courseId/contents/$contentId/?content_detail_v2=true"
    }
}
