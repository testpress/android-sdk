package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration38To39 {
    val MIGRATION_38_39: Migration = object : Migration(38, 39) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `ContentEntity` ADD COLUMN `learnlensAssetId` TEXT")
        }
    }
}
