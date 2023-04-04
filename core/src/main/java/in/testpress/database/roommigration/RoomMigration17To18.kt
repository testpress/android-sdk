package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration17To18 {
    val MIGRATION_17_18: Migration = object : Migration(17, 18) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS `RunningContentEntity`")
            database.execSQL("DROP TABLE IF EXISTS `RunningContentRemoteKeys`")
            database.execSQL("DROP TABLE IF EXISTS `UpcomingContentEntity`")
            database.execSQL("DROP TABLE IF EXISTS `UpcomingContentRemoteKeys`")
            database.execSQL("CREATE TABLE IF NOT EXISTS `ContentEntityLite` (`id` INTEGER NOT NULL, `type` INTEGER NOT NULL, `order` INTEGER, `chapterId` INTEGER, `freePreview` INTEGER, `title` TEXT, `courseId` INTEGER, `examId` INTEGER, `contentId` INTEGER, `videoId` INTEGER, `attachmentId` INTEGER, `contentType` TEXT, `icon` TEXT, `start` TEXT, `end` TEXT, `treePath` TEXT, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `ContentEntityLiteRemoteKey` (`contentId` INTEGER NOT NULL, `prevKey` INTEGER, `nextKey` INTEGER, `courseId` INTEGER NOT NULL, `type` INTEGER NOT NULL, PRIMARY KEY(`contentId`))")
        }
    }
}