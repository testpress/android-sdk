package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration35To36 {
    val MIGRATION_35_36: Migration = object : Migration(35, 36) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `OfflineAttachment` (`id` INTEGER NOT NULL, `title` TEXT NOT NULL, `url` TEXT NOT NULL, `path` TEXT NOT NULL, `status` TEXT NOT NULL, `progress` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        }
    }
}