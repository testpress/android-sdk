package `in`.testpress.exam.domain

import `in`.testpress.models.greendao.AttemptSection

data class DomainAttemptSection(
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
    var info: DomainSection? = null
)

fun AttemptSection.asDomainModel() = DomainAttemptSection(
    id = this.id,
    attemptSectionId = attemptSectionId,
    state = this.state,
    questionsUrl = questionsUrl,
    startUrl = startUrl,
    endUrl = endUrl,
    remainingTime = remainingTime,
    name = name,
    duration = duration,
    order = order,
    instructions = instructions,
    attemptId = attemptId
)

fun List<AttemptSection>.asDomainModels(): List<DomainAttemptSection> =
    map { it.asDomainModel() }
