package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration29To30 {
    val MIGRATION_29_30: Migration = object : Migration(29, 30) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS ContentLiteDao")
            database.execSQL("DELETE FROM ContentLiteRemoteKeyDao")
        }
    }
}