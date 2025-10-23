package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.util.PdfCacheManager
import `in`.testpress.course.util.EnhancedPdfProvider
import android.os.Bundle
import android.util.Log
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
        private const val ARG_PDF_PATH = "pdfPath"
    }
    
    private lateinit var pdfCacheManager: PdfCacheManager
    private var pdfId: String? = null
    
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
        val pdfPath = requireArguments().getString(ARG_PDF_PATH, "")
        
        if (contentId == -1L || courseId == -1L) {
            throw IllegalArgumentException("Required arguments (contentId, courseId) are missing or invalid.")
        }
        
        // Initialize PDF cache manager
        pdfCacheManager = PdfCacheManager.getInstance(requireContext())
        
        // Register PDF for streaming if path is provided
        if (pdfPath.isNotEmpty()) {
            try {
                pdfId = pdfCacheManager.registerPdf(pdfPath)
                Log.d("AIChatPdfFragment", "Registered PDF for streaming with ID: $pdfId")
            } catch (e: Exception) {
                Log.e("AIChatPdfFragment", "Failed to register PDF for streaming", e)
                // Fall back to original URL-based approach
            }
        }
        
        val webViewFragment = WebViewFragment()
        
        val pdfUrl = getPdfUrl(courseId, contentId)
    
        webViewFragment.arguments = Bundle().apply {
            putString(URL_TO_OPEN, pdfUrl)
            putBoolean(IS_AUTHENTICATION_REQUIRED, true)
        }
    
        webViewFragment.setListener(object : WebViewFragment.Listener {
            override fun onWebViewInitializationSuccess() {
                setupJavaScriptInterface(webViewFragment)
            }
        })

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

    private fun setupJavaScriptInterface(webViewFragment: WebViewFragment) {
        val pdfPath = requireArguments().getString(ARG_PDF_PATH, "")
        Log.d("AIChatPdfFragment", "Setting up JavaScript interface with PDF path: $pdfPath")
        
        webViewFragment.webView.settings.apply {
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
        }
        
        // Create enhanced JavaScript interface that provides streaming URL
        val jsInterface = EnhancedPdfProvider(requireActivity(), pdfPath, pdfId)
        webViewFragment.addJavascriptInterface(jsInterface, "AndroidPdfCache")
        
        Log.d("AIChatPdfFragment", "Enhanced JavaScript interface 'AndroidPdfCache' added successfully")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up registered PDF
        pdfId?.let { pdfCacheManager.unregisterPdf(it) }
    }
}
