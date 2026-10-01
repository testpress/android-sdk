package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration47To48 {
    @JvmField
    val MIGRATION_47_48: Migration = object : Migration(47, 48) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE ProductEntity ADD COLUMN purchaseState TEXT")
        }
    }
}
