package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration22To23 {
    val MIGRATION_22_23: Migration = object : Migration(22, 23) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS OfflineExam")
            database.execSQL("CREATE TABLE IF NOT EXISTS `OfflineExam` (`id` INTEGER, `totalMarks` TEXT, `url` TEXT, `attemptsCount` INTEGER, `pausedAttemptsCount` INTEGER, `title` TEXT, `description` TEXT, `startDate` TEXT, `endDate` TEXT, `duration` TEXT, `numberOfQuestions` INTEGER, `negativeMarks` TEXT, `markPerQuestion` TEXT, `templateType` INTEGER, `allowRetake` INTEGER, `allowPdf` INTEGER, `showAnswers` INTEGER, `maxRetakes` INTEGER, `attemptsUrl` TEXT, `deviceAccessControl` TEXT, `commentsCount` INTEGER, `slug` TEXT, `selectedLanguage` TEXT, `variableMarkPerQuestion` INTEGER, `passPercentage` INTEGER, `enableRanks` INTEGER, `showScore` INTEGER, `showPercentile` INTEGER, `categories` TEXT, `isDetailsFetched` INTEGER, `isGrowthHackEnabled` INTEGER, `shareTextForSolutionUnlock` TEXT, `showAnalytics` INTEGER, `instructions` TEXT, `hasAudioQuestions` INTEGER, `rankPublishingDate` TEXT, `enableQuizMode` INTEGER, `disableAttemptResume` INTEGER, `allowPreemptiveSectionEnding` INTEGER, `examDataModifiedOn` TEXT, `isSyncRequired` INTEGER NOT NULL, `contentId` INTEGER, PRIMARY KEY(`id`))")
        }
    }
}
