package `in`.testpress.course.fragments

import `in`.testpress.course.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.fragments.WebViewFragment.Companion.BASE_URL
import `in`.testpress.fragments.WebViewFragment.Companion.DATA_TO_OPEN
import `in`.testpress.fragments.WebViewFragment.Companion.IS_AUTHENTICATION_REQUIRED
import `in`.testpress.fragments.WebViewFragment.Companion.SHOW_LOADING_BETWEEN_PAGES
import `in`.testpress.course.util.LearnLensAssetManager
import java.io.File


class AIChatPdfFragment : Fragment() {
    
    companion object {
        private const val TAG = "AIChatPdfFragment"
        private const val ARG_CONTENT_ID = "contentId"
        private const val ARG_COURSE_ID = "courseId"
        private const val ARG_PDF_URL = "pdfUrl"
        private const val ARG_PDF_TITLE = "pdfTitle"
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
        
        // Generate LearnLens HTML
        val pdfUrl = requireArguments().getString(ARG_PDF_URL) 
            ?: throw IllegalArgumentException("PDF URL is required")
        val pdfTitle = requireArguments().getString(ARG_PDF_TITLE) ?: "PDF Document"
        val pdfId = "content-$contentId"
        val authToken = getAuthToken()
        
        val learnLensHtml = LearnLensAssetManager.generateLearnLensHtml(
            requireContext(),
            pdfUrl,
            pdfTitle,
            pdfId,
            authToken
        )
    
        val cacheDir = File(requireContext().filesDir, "learnlens_cache")
        webViewFragment.arguments = Bundle().apply {
            putString(DATA_TO_OPEN, learnLensHtml)
            putString(BASE_URL, "file://${cacheDir.absolutePath}/")
            putBoolean(IS_AUTHENTICATION_REQUIRED, false)
            putBoolean(SHOW_LOADING_BETWEEN_PAGES, false)
        }
        
        childFragmentManager.beginTransaction()
            .replace(R.id.aiPdf_view_fragment, webViewFragment)
            .commit()
    }
    
    private fun getAuthToken(): String {
        // TODO: Replace with real auth token when LearnLens supports it
        return "dummy-auth-token-for-testing"
    }
}
