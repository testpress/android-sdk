package `in`.testpress.course.domain


data class DomainContentArtifact(
    val id: Long,
    val name: String,
    val url: String? = null,
    val accessibleWithoutAttempt: Boolean = false
) {
    val isAccessible: Boolean get() = url != null
}
