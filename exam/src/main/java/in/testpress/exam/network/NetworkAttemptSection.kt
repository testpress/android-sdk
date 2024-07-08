package `in`.testpress.exam.network

import `in`.testpress.database.entities.OfflineAttemptSection
import `in`.testpress.exam.domain.DomainAttemptSection
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

fun NetworkAttemptSection.asDomainModel() = DomainAttemptSection(
        id = this.id,
        attemptSectionId = this.attemptSectionId,
        state = this.state,
        questionsUrl = this.questionsUrl,
        startUrl = this.startUrl,
        endUrl = this.endUrl,
        remainingTime = this.remainingTime,
        name = this.name,
        duration = this.duration,
        order = this.order,
        instructions = this.instructions,
        this.attemptId
)

fun List<NetworkAttemptSection>.asDomainModels(): List<DomainAttemptSection> =
        map { it.asDomainModel() }

fun OfflineAttemptSection.asNetworkModel(): NetworkAttemptSection {
        return NetworkAttemptSection(
                id = this.id,
                attemptSectionId = this.attemptSectionId,
                state = this.state,
                questionsUrl = "",
                startUrl = "",
                endUrl = "",
                remainingTime = this.remainingTime,
                name = this.name,
                duration = this.duration,
                order = this.order,
                instructions = this.instructions,
                attemptId = this.attemptId,
                info = null
        )
}