package `in`.testpress.course.ui.fragments

import `in`.testpress.course.R
import `in`.testpress.course.ui.fragments.content_fragments.BaseContentDetailFragment
import `in`.testpress.course.ui.view_models.ContentViewModel
import `in`.testpress.models.greendao.Content
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class SampleConcreteContentFragment : BaseContentDetailFragment() {
    public override lateinit var viewModel: ContentViewModel

    override var isBookmarkEnabled: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.attachment_content_detail, container, false);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ContentViewModel::class.java)
    }

    override fun loadContent() {

    }

    override fun onUpdateContent(content: Content) {
    }

}