package `in`.testpress.course.viewmodels

import `in`.testpress.course.domain.DomainOfflineVideo
import `in`.testpress.course.domain.asDomainModel
import `in`.testpress.course.repository.OfflineVideoRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class OfflineVideoViewModel(private val offlineVideoRepository: OfflineVideoRepository): ViewModel() {
    val offlineVideos = offlineVideoRepository.offlineVideos.map() {
        it.asDomainModel()
    }

    fun get(url: String): LiveData<DomainOfflineVideo?> {
        return offlineVideoRepository.get(url).map() {
            it?.asDomainModel()
        }
    }
}