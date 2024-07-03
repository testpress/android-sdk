package `in`.testpress.database.mapping

import `in`.testpress.database.entities.OfflineAttemptSection
import `in`.testpress.models.greendao.AttemptSection
import android.util.Log

fun OfflineAttemptSection?.asGreenDoaModel(): AttemptSection? {
    if (this == null) return null
    val attemptSection = AttemptSection()
    attemptSection.id = this.id
    attemptSection.state = this.state
    attemptSection.questionsUrl = null
    attemptSection.startUrl = null
    attemptSection.endUrl = null
    attemptSection.remainingTime = this.remainingTime
    Log.d("TAG", "asGreenDoaModel: ${attemptSection.name}")
    attemptSection.name = this.name
    Log.d("TAG", "asGreenDoaModel: ${this.name}")
    Log.d("TAG", "asGreenDoaModel: ${attemptSection.name}")
    attemptSection.duration = this.duration
    attemptSection.order = this.order
    attemptSection.instructions = this.instructions
    attemptSection.attemptId = this.attemptId
    return attemptSection
}

fun List<OfflineAttemptSection?>.asGreenDoaModels(): List<AttemptSection?> {
    return this.map {
        it?.asGreenDoaModel()
    }
}