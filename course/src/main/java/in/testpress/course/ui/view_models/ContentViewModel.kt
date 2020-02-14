package `in`.testpress.course.ui.view_models

import `in`.testpress.models.greendao.Content
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel


class ContentViewModel : ViewModel() {
    private lateinit var content: LiveData<Content>

    public fun fetchContent():LiveData<Content> {
        content?.let {
            content = MutableLiveData<Content>()
            loadContent()
        }
        return content
    }

    private fun loadContent() {

    }
}