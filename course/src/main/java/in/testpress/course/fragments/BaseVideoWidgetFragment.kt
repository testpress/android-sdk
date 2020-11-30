package `in`.testpress.course.fragments

import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.di.InjectorUtils
import `in`.testpress.course.viewmodels.ContentViewModel
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

open class BaseVideoWidgetFragment : Fragment() {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    lateinit var viewModel: ContentViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentType = requireArguments().getString(TestpressCourse.CONTENT_TYPE)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ContentViewModel(
                    InjectorUtils.getContentRepository(contentType!!, context!!)
                ) as T
            }
        }).get(ContentViewModel::class.java)
    }

    open fun seekTo(milliSeconds: Long?) {}

    open fun fastForward(milliSeconds: Long?) {}

    open fun backward(milliSeconds: Long?) {}

    open fun animateFastForward() {}

    open fun animateBackward() {}
}