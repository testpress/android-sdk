package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration39To40 {
    val MIGRATION_39_40: Migration = object : Migration(39, 40) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `VideoQuestion` " +
                "(`videoContentId` INTEGER NOT NULL PRIMARY KEY, " +
                "`questionsJson` TEXT NOT NULL)"
            )
        }
    }
}

