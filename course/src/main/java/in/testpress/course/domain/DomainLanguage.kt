package `in`.testpress.course.domain

import `in`.testpress.models.greendao.Language

data class DomainLanguage(
    val id: Long,
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

fun List<Language>.asDomainLanguages(): List<DomainLanguage> {
    return this.map {
        it.asDomainLanguage()
    }
}

fun Language.asDomainLanguage(): DomainLanguage {
    return createDomainLanguage(this)
}