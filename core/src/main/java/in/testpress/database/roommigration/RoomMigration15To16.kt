package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration15To16 {

    val MIGRATION_15_16: Migration = object : Migration(15, 16) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `UpcomingContentEntity` (`id` INTEGER NOT NULL, `order` INTEGER, `chapterId` INTEGER, `freePreview` INTEGER, `title` TEXT, `courseId` INTEGER, `examId` INTEGER, `contentId` INTEGER, `videoId` INTEGER, `attachmentId` INTEGER, `contentType` TEXT, `icon` TEXT, `start` TEXT, `end` TEXT, `treePath` TEXT, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `RunningContentEntity` (`id` INTEGER NOT NULL, `order` INTEGER, `chapterId` INTEGER, `freePreview` INTEGER, `title` TEXT, `courseId` INTEGER, `examId` INTEGER, `contentId` INTEGER, `videoId` INTEGER, `attachmentId` INTEGER, `contentType` TEXT, `icon` TEXT, `start` TEXT, `end` TEXT, `treePath` TEXT, PRIMARY KEY(`id`))")
        }
    }
}