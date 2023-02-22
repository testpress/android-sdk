package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.RunningContentsRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn

class RunningContentsListViewModel(val repository: RunningContentsRepository) : ViewModel() {

    val runningContentList =repository.runningContentList().cachedIn(viewModelScope)
}