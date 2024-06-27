package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration23To24 {
    val MIGRATION_23_24: Migration = object : Migration(23, 24) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `OfflineCourseAttempt` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `assessmentId` INTEGER NOT NULL)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `OfflineAttempt` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `totalQuestions` INTEGER NOT NULL, `lastStartedTime` TEXT NOT NULL, `remainingTime` TEXT NOT NULL, `timeTaken` TEXT NOT NULL, `state` TEXT NOT NULL, `attemptType` INTEGER NOT NULL)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `OfflineAttemptSection` (`id` INTEGER NOT NULL, `attemptSectionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `state` TEXT NOT NULL, `remainingTime` TEXT, `name` TEXT NOT NULL, `duration` TEXT NOT NULL, `order` INTEGER NOT NULL, `instructions` TEXT, `attemptId` INTEGER NOT NULL)")
        }
    }
}
