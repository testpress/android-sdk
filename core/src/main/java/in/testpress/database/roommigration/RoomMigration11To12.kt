package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration11To12 {
    val MIGRATION_11_12: Migration = object : Migration(11, 12) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `OfflineVideo` ADD COLUMN `lastWatchPosition` TEXT");
            database.execSQL("ALTER TABLE `OfflineVideo` ADD COLUMN `watchedTimeRanges` TEXT NOT NULL DEFAULT '[]'");
            database.execSQL("ALTER TABLE `OfflineVideo` ADD COLUMN `syncState` TEXT NOT NULL DEFAULT 'NOT_SYNCED'");
        }
    }
}