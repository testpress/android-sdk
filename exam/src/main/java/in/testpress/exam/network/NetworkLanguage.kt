package `in`.testpress.exam.network

import `in`.testpress.models.greendao.Language

data class NetworkLanguage(
    val id: Long,
    val title: String? = null,
    val code: String? = null,
    var examId: Long? = null
)

fun createGreenDaoModel(language: NetworkLanguage): Language {
    return Language(
        language.id,
        language.code,
        language.title,
        language.examId
    )
}
fun NetworkLanguage.asGreenDaoModel(): Language {
    return createGreenDaoModel(this)
}

fun List<NetworkLanguage>.asGreenDaoModels(): List<Language> {
    return this.map {
        createGreenDaoModel(it)
    }
}