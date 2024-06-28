package `in`.testpress.exam.util

import `in`.testpress.database.entities.*
import `in`.testpress.database.mapping.asGreenDoaModel
import `in`.testpress.exam.models.AttemptAnswer
import `in`.testpress.exam.models.AttemptItem
import `in`.testpress.exam.models.AttemptQuestion
import `in`.testpress.exam.models.UserUploadedFile

fun OfflineAttemptItem.asAttemptItem(subject: String, direction: String?): AttemptItem{
    return AttemptItem(
        id.toInt(),
        null,
        question.asAttemptQuestion(subject, direction),
        selectedAnswers,
        review,
       savedAnswers,
        order,
        null,
        shortText,
        currentShortText,
        attemptSection?.asGreenDoaModel(),
        essayText,
        localEssayText,
        files.asUserUploadedFiles(),
        unSyncedFiles
    )
}

fun Question.asAttemptQuestion(subject: String, direction: String?): AttemptQuestion {
    return AttemptQuestion(
        questionHtml,
        answers.asAttemptAnswers(),
        subject,
        direction,
        type,
        language,
        translations.asAttemptQuestions(subject, direction) as ArrayList<AttemptQuestion>,
        marks,
        negativeMarks,
    )
}

fun List<Question>.asAttemptQuestions(subject: String, direction: String?) = this.map { it.asAttemptQuestion(subject, direction) }

fun Answer.asAttemptAnswer(): AttemptAnswer {
    return AttemptAnswer(
        this.textHtml!!,
        this.id?.toInt()
    )
}

fun List<Answer>.asAttemptAnswers() = this.map { it.asAttemptAnswer() }

fun OfflineUserUploadedFile.asUserUploadedFile() = UserUploadedFile(id, url, path)

fun List<OfflineUserUploadedFile>.asUserUploadedFiles() = this.map { it.asUserUploadedFile() }