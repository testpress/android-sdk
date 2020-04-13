package `in`.testpress.course.domain

import `in`.testpress.exam.network.NetworkLanguage
import `in`.testpress.models.greendao.Language

data class DomainLanguage(
    val id: Long?,
    val title: String? = null,
    val code: String? = null,
    val examId: Long? = null
)

fun createDomainLanguage(language: Language): DomainLanguage {
    return DomainLanguage(
        id = language.id,
        title = language.title,
        code = language.code,
        examId = language.examId
    )
}

fun createDomainLanguage(language: NetworkLanguage): DomainLanguage {
    return DomainLanguage(
        id = language.id,
        title = language.title,
        code = language.code,
        examId = language.examId
    )
}

fun List<Language>.asDomainLanguages(): List<DomainLanguage> {
    return this.map {
        it.asDomainLanguage()
    }
}

fun Language.asDomainLanguage(): DomainLanguage {
    return createDomainLanguage(this)
}

fun NetworkLanguage.asDomainLanguage(): DomainLanguage {
    return createDomainLanguage(this)
}

fun List<NetworkLanguage>.toDomainLanguages(): List<DomainLanguage> {
    return this.map {
        createDomainLanguage(it)
    }
}

fun createGreenDaoModel(language: DomainLanguage): Language {
    return Language(
        language.id,
        language.code,
        language.title,
        language.examId
    )
}

fun List<DomainLanguage>.toGreenDaoModels(): List<Language> {
    return this.map {
        createGreenDaoModel(it)
    }
}