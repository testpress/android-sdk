package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.util.PdfWebViewCache
import `in`.testpress.core.TestpressSdk
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment

/**
 * Fragment that displays PDF using a cached WebView.
 * WebViews survive Activity destruction for instant return.
 */
class AIChatPdfFragment : Fragment() {
    
    companion object {
        private const val ARG_CONTENT_ID = "contentId"
        private const val ARG_COURSE_ID = "courseId"
    }
    
    private var webView: android.webkit.WebView? = null
    private var container: FrameLayout? = null
    
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
        
        container = view.findViewById(R.id.aiPdf_view_fragment)
        
        // Get PDF URL
        val pdfUrl = getPdfUrl(courseId, contentId)
        
        // Get cached WebView (or create new one)
        webView = PdfWebViewCache.acquire(contentId, pdfUrl)
        
        // Attach to container
        container?.let { PdfWebViewCache.attach(it, webView!!) }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        
        // Detach but don't destroy - stays in cache!
        PdfWebViewCache.detach(webView)
        
        webView = null
        container = null
    }

    private fun getPdfUrl(courseId: Long, contentId: Long): String {
        val session = TestpressSdk.getTestpressSession(requireContext()) 
            ?: throw IllegalStateException("User session not found.")
        val baseUrl = session.instituteSettings?.domainUrl.takeIf { !it.isNullOrEmpty() }
            ?: throw IllegalStateException("Base URL not configured.")
        return "$baseUrl/courses/$courseId/contents/$contentId/?content_detail_v2=true"
    }
}
