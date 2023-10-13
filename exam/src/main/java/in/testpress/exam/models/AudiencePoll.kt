package `in`.testpress.exam.models


data class AudiencePollResponse(
    val coins_changed: Int?,
    val audience_poll: List<AudiencePoll?>?
)

data class AudiencePoll(
    val text_html: String?,
    val id: Int?,
    val poll_percent: String?
)