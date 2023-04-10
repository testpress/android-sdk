package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.CourseContentsRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn

class CourseContentListViewModel(val repository: CourseContentsRepository) : ViewModel() {

    val courseContentList =repository.courseContentList().cachedIn(viewModelScope)
}