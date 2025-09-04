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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    
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
        
        val contentId = arguments?.getLong(ARG_CONTENT_ID, -1L) ?: -1L
        val courseId = arguments?.getLong(ARG_COURSE_ID, -1L) ?: -1L
        
        if (contentId == -1L || courseId == -1L) {
            return
        }
        
        val webViewFragment = WebViewFragment()
        
        val session: TestpressSession? = TestpressSdk.getTestpressSession(requireContext())
        val baseUrl = session?.instituteSettings?.baseUrl ?: ""
        
        val pdfUrl = "$baseUrl/courses/$courseId/contents/$contentId/?content_detail_v2=true"
    
        webViewFragment.arguments = Bundle().apply {
            putString(URL_TO_OPEN, pdfUrl)
            putBoolean(IS_AUTHENTICATION_REQUIRED, true)
        }
    
        childFragmentManager.beginTransaction()
            .replace(R.id.aiPdf_view_fragment, webViewFragment)
            .commit()
    }
}
