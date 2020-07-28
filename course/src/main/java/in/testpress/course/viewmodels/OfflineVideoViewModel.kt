package `in`.testpress.course.viewmodels

import `in`.testpress.course.domain.DomainOfflineVideo
import `in`.testpress.course.domain.asDomainModel
import `in`.testpress.course.repository.OfflineVideoRepository
import `in`.testpress.util.RefreshableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class OfflineVideoViewModel(private val offlineVideoRepository: OfflineVideoRepository): ViewModel() {
    private val _offlineVideos = RefreshableLiveData {
        offlineVideoRepository.offlineVideos
    }
    val offlineVideos = Transformations.map(_offlineVideos) {
        it.asDomainModel()
    }

    val urls = offlineVideoRepository.getUrls()

    fun get(url: String): LiveData<DomainOfflineVideo?> {
        return Transformations.map(offlineVideoRepository.get(url)) {
            it?.asDomainModel()
        }
    }

    fun refresh() {
        _offlineVideos.refresh()
    }
}