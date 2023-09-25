package `in`.testpress.exam.network

import `in`.testpress.models.greendao.AttemptSection
import `in`.testpress.models.greendao.CourseAttempt

data class NetworkAttemptSection(
        val id: Long,
        val attemptSectionId: Long,
        var state: String? = null,
        val questionsUrl: String? = null,
        val startUrl: String? = null,
        val endUrl: String? = null,
        val remainingTime: String? = null,
        val name: String? = null,
        val duration: String? = null,
        val order: Int? = null,
        val instructions: String? = null,
        val attemptId: Long? = null,
        var info: NetworkSection? = null
)


fun List<NetworkAttemptSection>.asGreenDaoModel(): List<AttemptSection> {
    return this.map {
        createAttemptSection(it)
    }
}

fun createAttemptSection(networkAttemptSection: NetworkAttemptSection): AttemptSection {
        return AttemptSection(
                networkAttemptSection.id,
                networkAttemptSection.attemptSectionId,
                networkAttemptSection.state,
                networkAttemptSection.questionsUrl,
                networkAttemptSection.startUrl,
                networkAttemptSection.endUrl,
                networkAttemptSection.remainingTime,
                networkAttemptSection.name ?: networkAttemptSection.info?.name,
                networkAttemptSection.duration ?: networkAttemptSection.info?.duration,
                networkAttemptSection.order ?: networkAttemptSection.info?.order,
                networkAttemptSection.instructions ?: networkAttemptSection.info?.instructions,
                networkAttemptSection.attemptId
        )
}
