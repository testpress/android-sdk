package `in`.testpress.exam.network

import `in`.testpress.models.greendao.Language

data class NetworkLanguage(
    val id: Long,
    val title: String? = null,
    val code: String? = null,
    val examId: Long? = null
)

fun NetworkLanguage.asGreenDaoModel(): Language {
    return Language(
        this.id,
        this.code,
        this.title,
        this.examId
    )
}
