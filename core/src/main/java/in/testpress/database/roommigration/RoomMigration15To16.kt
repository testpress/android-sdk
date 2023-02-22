package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration15To16 {
    val MIGRATION_15_16: Migration = object : Migration(15, 16) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS `ContentEntity`")
            database.execSQL("CREATE TABLE IF NOT EXISTS `ContentEntity` (`id` INTEGER NOT NULL, `description` TEXT, `image` TEXT, `url` TEXT NOT NULL, `chapterSlug` TEXT NOT NULL, `chapterUrl` TEXT, `modified` TEXT, `examUrl` TEXT, `videoUrl` TEXT, `attachmentUrl` TEXT, `htmlUrl` TEXT, `isLocked` INTEGER NOT NULL, `isScheduled` INTEGER NOT NULL, `attemptsCount` INTEGER NOT NULL, `bookmarkId` INTEGER, `videoWatchedPercentage` INTEGER, `active` INTEGER NOT NULL, `htmlId` INTEGER, `hasStarted` INTEGER NOT NULL, `isCourseAvailable` INTEGER, `coverImageSmall` TEXT, `coverImageMedium` TEXT, `coverImage` TEXT, `nextContentId` INTEGER, `hasEnded` INTEGER, `examStartUrl` TEXT, `order` INTEGER, `chapterId` INTEGER, `freePreview` INTEGER, `title` TEXT, `courseId` INTEGER, `examId` INTEGER, `contentId` INTEGER, `videoId` INTEGER, `attachmentId` INTEGER, `contentType` TEXT, `icon` TEXT, `start` TEXT, `end` TEXT, `treePath` TEXT, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `RunningContentEntity` (`id` INTEGER NOT NULL, `order` INTEGER, `chapterId` INTEGER, `freePreview` INTEGER, `title` TEXT, `courseId` INTEGER, `examId` INTEGER, `contentId` INTEGER, `videoId` INTEGER, `attachmentId` INTEGER, `contentType` TEXT, `icon` TEXT, `start` TEXT, `end` TEXT, `treePath` TEXT, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `UpcomingContentEntity` (`id` INTEGER NOT NULL, `order` INTEGER, `chapterId` INTEGER, `freePreview` INTEGER, `title` TEXT, `courseId` INTEGER, `examId` INTEGER, `contentId` INTEGER, `videoId` INTEGER, `attachmentId` INTEGER, `contentType` TEXT, `icon` TEXT, `start` TEXT, `end` TEXT, `treePath` TEXT, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `RunningContentRemoteKeys` (`contentId` INTEGER NOT NULL, `prevKey` INTEGER, `nextKey` INTEGER, `courseId` INTEGER NOT NULL, PRIMARY KEY(`contentId`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `UpcomingContentRemoteKeys` (`contentId` INTEGER NOT NULL, `prevKey` INTEGER, `nextKey` INTEGER, `courseId` INTEGER NOT NULL, PRIMARY KEY(`contentId`))")
        }
    }
}