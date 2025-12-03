package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration41To42 {
    val MIGRATION_41_42: Migration = object : Migration(41, 42) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `HighlightEntity` " +
                "(`id` INTEGER NOT NULL, " +
                "`contentId` INTEGER NOT NULL, " +
                "`pageNumber` INTEGER, " +
                "`selectedText` TEXT, " +
                "`notes` TEXT, " +
                "`color` TEXT, " +
                "`position` TEXT, " +
                "`created` TEXT, " +
                "`modified` TEXT, " +
                "`lastUpdated` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))"
            )
            
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_HighlightEntity_contentId` " +
                "ON `HighlightEntity`(`contentId`)"
            )
        }
    }
}



