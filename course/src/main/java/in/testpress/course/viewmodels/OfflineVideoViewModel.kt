package `in`.testpress.course.viewmodels

import `in`.testpress.course.domain.DomainOfflineVideo
import `in`.testpress.course.domain.asDomainModel
import `in`.testpress.course.repository.OfflineVideoRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class OfflineVideoViewModel(private val offlineVideoRepository: OfflineVideoRepository): ViewModel() {
    val offlineVideos = Transformations.map(offlineVideoRepository.offlineVideos) {
        it.asDomainModel()
    }

    fun get(url: String): LiveData<DomainOfflineVideo?> {
        return Transformations.map(offlineVideoRepository.get(url)) {
            it?.asDomainModel()
        }
    }
}