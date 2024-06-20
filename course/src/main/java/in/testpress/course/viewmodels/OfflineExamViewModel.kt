package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.OfflineExamRepository
import androidx.lifecycle.ViewModel

class OfflineExamViewModel(private val offlineExamViewModel: OfflineExamRepository): ViewModel() {

    fun fetch(contentId: Long) {
        offlineExamViewModel.fetch(contentId)
    }
}