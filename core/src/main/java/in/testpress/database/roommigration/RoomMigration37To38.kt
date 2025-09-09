package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration37To38 {
    val MIGRATION_37_38: Migration = object : Migration(37, 38) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `ContentEntity` ADD COLUMN `isAIEnabled` INTEGER")
        }
    }
}
