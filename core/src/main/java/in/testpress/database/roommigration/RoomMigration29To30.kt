package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration29To30 {
    val MIGRATION_29_30: Migration = object : Migration(29, 30) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS ContentLiteDao")
            database.execSQL("DELETE FROM ContentLiteRemoteKeyDao")
            database.execSQL("CREATE TABLE IF NOT EXISTS `RunningContentEntity` (`contentOrder` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `id` INTEGER NOT NULL, `type` INTEGER NOT NULL, `order` INTEGER, `chapterId` INTEGER, `freePreview` INTEGER, `title` TEXT, `courseId` INTEGER, `examId` INTEGER, `contentId` INTEGER, `videoId` INTEGER, `attachmentId` INTEGER, `liveStreamId` INTEGER, `contentType` TEXT, `icon` TEXT, `start` TEXT, `end` TEXT, `treePath` TEXT)")
        }
    }
}