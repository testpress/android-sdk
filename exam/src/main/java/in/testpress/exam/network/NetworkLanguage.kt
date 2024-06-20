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

fun List<NetworkLanguage>.asRoomModels(examId: Long): List<`in`.testpress.database.entities.Language>{
    return this.map {
        it.asRoomModel(examId)
    }
}

fun NetworkLanguage.asRoomModel(examId: Long): `in`.testpress.database.entities.Language{
    return `in`.testpress.database.entities.Language(
        id = this.id,
        code = this.code,
        title = this.title,
        examId = examId
    )
}