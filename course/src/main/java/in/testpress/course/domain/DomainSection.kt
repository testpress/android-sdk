package `in`.testpress.course.domain

data class DomainSection(
    val order: Int? = null,
    val name: String = "",
    val duration: String = "",
    val cutOff: Int = 0,
    val instructions: String = ""
)
