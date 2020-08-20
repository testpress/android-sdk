package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.CourseRepository
import androidx.lifecycle.ViewModel

class CourseViewModel(val repository: CourseRepository): ViewModel() {
    val courses = repository.resourceCourses

    fun load() {
        repository.fetch()
    }
}