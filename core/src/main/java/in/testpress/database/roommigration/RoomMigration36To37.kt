package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration36To37 {
    val MIGRATION_36_37: Migration = object : Migration(36, 37) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS OfflineAttachment")
            database.execSQL("CREATE TABLE IF NOT EXISTS `OfflineAttachment` (`id` INTEGER NOT NULL, `title` TEXT NOT NULL, `url` TEXT NOT NULL, `path` TEXT NOT NULL, `contentUri` TEXT, `downloadId` INTEGER NOT NULL, `status` TEXT NOT NULL, `progress` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        }
    }
}