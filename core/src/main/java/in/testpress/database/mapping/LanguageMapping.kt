package `in`.testpress.database.mapping

import `in`.testpress.database.entities.Language

fun Language.asGreenDaoModel(): `in`.testpress.models.greendao.Language {
    return `in`.testpress.models.greendao.Language(
        this.id, this.code, this.title, this.examId
    )
}

fun List<Language>.asGreenDaoModels(): List<`in`.testpress.models.greendao.Language> {
    return this.map {
        it.asGreenDaoModel()
    }
}