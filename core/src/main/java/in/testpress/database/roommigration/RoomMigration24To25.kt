package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration24To25 {
    val MIGRATION_24_25: Migration = object : Migration(24, 25) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS OfflineAttemptSection")
            database.execSQL("CREATE TABLE IF NOT EXISTS `OfflineAttemptSection` (`id` INTEGER NOT NULL, `attemptSectionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `state` TEXT NOT NULL, `remainingTime` TEXT, `name` TEXT, `duration` TEXT, `order` INTEGER NOT NULL, `instructions` TEXT, `attemptId` INTEGER NOT NULL)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `OfflineAttemptItem` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `question` TEXT NOT NULL, `selectedAnswers` TEXT NOT NULL, `review` INTEGER, `savedAnswers` TEXT NOT NULL, `order` INTEGER NOT NULL, `shortText` TEXT, `currentShortText` TEXT, `attemptSection` TEXT, `essayText` TEXT, `localEssayText` TEXT, `files` TEXT NOT NULL, `unSyncedFiles` TEXT NOT NULL)")
        }
    }
}