package `in`.testpress.database.mapping

import `in`.testpress.database.entities.OfflineExam
import `in`.testpress.models.greendao.Exam

fun OfflineExam.asGreenDaoModel(): Exam {
    return Exam(
        totalMarks,
        this.url,
        this.id,
        this.attemptsCount,
        this.pausedAttemptsCount,
        this.title,
        this.description,
        this.startDate,
        this.endDate,
        this.duration,
        this.numberOfQuestions,
        this.negativeMarks,
        this.markPerQuestion,
        this.templateType,
        this.allowRetake,
        this.allowPdf,
        this.showAnswers,
        this.maxRetakes,
        this.attemptsUrl,
        this.deviceAccessControl,
        this.commentsCount,
        this.slug,
        null,
        this.variableMarkPerQuestion,
        this.passPercentage,
        this.enableRanks,
        this.showScore,
        this.showPercentile,
        null,
        null,
        this.isGrowthHackEnabled,
        this.shareTextForSolutionUnlock,
        this.showAnalytics,
        this.instructions,
        this.hasAudioQuestions,
        this.rankPublishingDate,
        this.enableQuizMode,
        this.disableAttemptResume,
        this.allowPreemptiveSectionEnding,
        this.examDataModifiedOn,
        true,
        this.graceDurationForOfflineSubmission
    )
}