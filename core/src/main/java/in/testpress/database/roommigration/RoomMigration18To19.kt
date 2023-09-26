package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration18To19 {
    val MIGRATION_18_19: Migration = object : Migration(18, 19) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `ContentEntity` ADD COLUMN `liveStreamId` INTEGER");
            database.execSQL("ALTER TABLE `RunningContentEntity` ADD COLUMN `liveStreamId` INTEGER");
            database.execSQL("ALTER TABLE `UpcomingContentEntity` ADD COLUMN `liveStreamId` INTEGER");
        }
    }
}