package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration19To20 {
    val MIGRATION_19_20: Migration = object : Migration(19, 20) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `OfflineExam` (`id` INTEGER, `totalMarks` TEXT, `url` TEXT, `attemptsCount` INTEGER, `pausedAttemptsCount` INTEGER, `title` TEXT, `description` TEXT, `startDate` TEXT, `endDate` TEXT, `duration` TEXT, `numberOfQuestions` INTEGER, `negativeMarks` TEXT, `markPerQuestion` TEXT, `templateType` INTEGER, `allowRetake` INTEGER, `allowPdf` INTEGER, `showAnswers` INTEGER, `maxRetakes` INTEGER, `attemptsUrl` TEXT, `deviceAccessControl` TEXT, `commentsCount` INTEGER, `slug` TEXT, `selectedLanguage` TEXT, `variableMarkPerQuestion` INTEGER, `passPercentage` INTEGER, `enableRanks` INTEGER, `showScore` INTEGER, `showPercentile` INTEGER, `categories` TEXT, `isDetailsFetched` INTEGER, `isGrowthHackEnabled` INTEGER, `shareTextForSolutionUnlock` TEXT, `showAnalytics` INTEGER, `instructions` TEXT, `hasAudioQuestions` INTEGER, `rankPublishingDate` TEXT, `enableQuizMode` INTEGER, `disableAttemptResume` INTEGER, `allowPreemptiveSectionEnding` INTEGER, `examDataModifiedOn` TEXT, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `Section` (`id` INTEGER, `order` INTEGER, `name` TEXT, `duration` TEXT, `cutOff` INTEGER, `instructions` TEXT, `parent` INTEGER, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `Language` (`id` INTEGER, `code` TEXT, `title` TEXT, `examId` INTEGER, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `ExamQuestion` (`id` INTEGER, `order` INTEGER, `examId` INTEGER, `attemptId` INTEGER, `questionId` INTEGER, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `Answer` (`id` INTEGER, `textHtml` TEXT, `marks` TEXT, `questionId` INTEGER, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `Direction` (`id` INTEGER, `html` TEXT, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `Subject` (`id` INTEGER, `name` TEXT, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `Question` (`id` INTEGER, `questionHtml` TEXT, `parentId` INTEGER, `type` TEXT, `subjectId` INTEGER, `answerIds` TEXT, `directionId` INTEGER, `translations` TEXT NOT NULL, PRIMARY KEY(`id`))")
        }
    }
}
