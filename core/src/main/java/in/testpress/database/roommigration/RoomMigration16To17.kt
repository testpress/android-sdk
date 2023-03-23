package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration16To17 {
    val MIGRATION_16_17: Migration = object : Migration(16, 17) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `RunningContentEntity` (`id` INTEGER NOT NULL, `order` INTEGER, `chapterId` INTEGER, `freePreview` INTEGER, `title` TEXT, `courseId` INTEGER, `examId` INTEGER, `contentId` INTEGER, `videoId` INTEGER, `attachmentId` INTEGER, `contentType` TEXT, `icon` TEXT, `start` TEXT, `end` TEXT, `treePath` TEXT, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `RunningContentRemoteKeys` (`contentId` INTEGER NOT NULL, `prevKey` INTEGER, `nextKey` INTEGER, `courseId` INTEGER NOT NULL, PRIMARY KEY(`contentId`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `UpcomingContentEntity` (`id` INTEGER NOT NULL, `order` INTEGER, `chapterId` INTEGER, `freePreview` INTEGER, `title` TEXT, `courseId` INTEGER, `examId` INTEGER, `contentId` INTEGER, `videoId` INTEGER, `attachmentId` INTEGER, `contentType` TEXT, `icon` TEXT, `start` TEXT, `end` TEXT, `treePath` TEXT, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `UpcomingContentRemoteKeys` (`contentId` INTEGER NOT NULL, `prevKey` INTEGER, `nextKey` INTEGER, `courseId` INTEGER NOT NULL, PRIMARY KEY(`contentId`))")
        }
    }
}