package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration9To10 {
    val MIGRATION_9_10: Migration = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS ContentEntity")
            database.execSQL("CREATE TABLE IF NOT EXISTS `ContentEntity` (`id` INTEGER NOT NULL, `title` TEXT, `description` TEXT, `image` TEXT, `order` INTEGER, `url` TEXT NOT NULL, `chapterId` INTEGER, `chapterSlug` TEXT NOT NULL, `chapterUrl` TEXT, `courseId` INTEGER, `freePreview` INTEGER, `modified` TEXT, `contentType` TEXT NOT NULL, `examUrl` TEXT, `videoUrl` TEXT, `attachmentUrl` TEXT, `htmlUrl` TEXT, `isLocked` INTEGER NOT NULL, `isScheduled` INTEGER NOT NULL, `attemptsCount` INTEGER NOT NULL, `bookmarkId` INTEGER, `videoWatchedPercentage` INTEGER, `active` INTEGER NOT NULL, `examId` INTEGER, `attachmentId` INTEGER, `videoId` INTEGER, `htmlId` INTEGER, `start` TEXT, `hasStarted` INTEGER NOT NULL, `isCourseAvailable` INTEGER, `coverImageSmall` TEXT, `coverImageMedium` TEXT, `coverImage` TEXT, `nextContentId` INTEGER, `hasEnded` INTEGER, PRIMARY KEY(`id`))")
        }
    }
}