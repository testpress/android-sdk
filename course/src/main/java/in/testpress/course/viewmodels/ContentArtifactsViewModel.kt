package `in`.testpress.course.viewmodels

import `in`.testpress.course.domain.DomainContentArtifact
import `in`.testpress.course.repository.ContentArtifactsRepository
import `in`.testpress.network.Resource
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class ContentArtifactsViewModel(
    private val repository: ContentArtifactsRepository
) : ViewModel() {

    fun loadArtifacts(contentId: Long): LiveData<Resource<List<DomainContentArtifact>>> {
        return repository.loadArtifacts(contentId)
    }
}
