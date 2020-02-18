package `in`.testpress.course.ui.fragments

import `in`.testpress.course.ui.fragments.content_fragments.BaseContentDetailFragment
import `in`.testpress.course.ui.view_models.ContentViewModel
import `in`.testpress.models.greendao.Content
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle

class SampleConcreteContentFragment : BaseContentDetailFragment() {
    public override lateinit var viewModel: ContentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ContentViewModel::class.java)
    }

    override fun loadContent() {

    }

    override fun onUpdateContent(content: Content) {
    }

}