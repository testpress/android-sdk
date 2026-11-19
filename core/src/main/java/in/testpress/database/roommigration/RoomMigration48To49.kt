package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration48To49 {
    @JvmField
    val MIGRATION_48_49: Migration = object : Migration(48, 49) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE ContentEntity ADD COLUMN `position` INTEGER")
        }
    }
}
