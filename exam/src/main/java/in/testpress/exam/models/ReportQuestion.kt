package `in`.testpress.exam.models

data class ReportQuestionResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val per_page: Int,
    val results: List<ReportQuestion>,
    val is_report_resolved: Boolean
) {
    inner class ReportQuestion(
        val id: Int,
        val user: User,
        val description: String,
        val type: Int,
        val type_display: String,
        val exam_id: Int,
        val created: String
    )

    inner class User(
        val id: Int,
        val url: String,
        val display_name: String,
        val first_name: String,
        val last_name: String,
        val photo: String,
        val large_image: String,
        val medium_image: String,
        val medium_small_image: String,
        val small_image: String,
        val x_small_image: String,
        val mini_image: String
    )
}