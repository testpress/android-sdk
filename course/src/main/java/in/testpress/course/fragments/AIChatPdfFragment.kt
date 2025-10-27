package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.util.WebViewFragmentCache
import `in`.testpress.core.TestpressSdk
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Fragment that displays PDF content using a cached WebViewFragment.
 * 
 * Reuses existing WebViewFragment instances for instant switching between PDFs.
 */
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
        
        // Get PDF URL
        val pdfUrl = getPdfUrl(courseId, contentId)
        
        // Get cached WebViewFragment or create new one
        val webViewFragment = WebViewFragmentCache.getOrCreate(contentId, pdfUrl)
        
        // Add once, then just show/hide
        val transaction = childFragmentManager.beginTransaction()
        
        if (!webViewFragment.isAdded) {
            // First time - add the fragment
            transaction.add(R.id.aiPdf_view_fragment, webViewFragment)
        } else if (webViewFragment.isDetached) {
            // Was detached before - reattach it
            transaction.attach(webViewFragment)
        }
        
        transaction.commit()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        
        // Detach (not remove!) so fragment stays alive in cache
        val contentId = requireArguments().getLong(ARG_CONTENT_ID, -1L)
        val pdfUrl = getPdfUrl(requireArguments().getLong(ARG_COURSE_ID, -1L), contentId)
        val webViewFragment = WebViewFragmentCache.getOrCreate(contentId, pdfUrl)
        
        if (webViewFragment.isAdded) {
            childFragmentManager.beginTransaction()
                .detach(webViewFragment)
                .commit()
        }
    }

    private fun getPdfUrl(courseId: Long, contentId: Long): String {
        val session = TestpressSdk.getTestpressSession(requireContext()) 
            ?: throw IllegalStateException("User session not found.")
        val baseUrl = session.instituteSettings?.domainUrl.takeIf { !it.isNullOrEmpty() }
            ?: throw IllegalStateException("Base URL not configured.")
        return "$baseUrl/courses/$courseId/contents/$contentId/?content_detail_v2=true"
    }
}
