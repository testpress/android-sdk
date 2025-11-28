package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration40To41 {
    val MIGRATION_40_41: Migration = object : Migration(40, 41) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `BookmarkEntity` " +
                "(`id` INTEGER NOT NULL, " +
                "`contentId` INTEGER NOT NULL, " +
                "`bookmarkType` TEXT NOT NULL, " +
                "`pageNumber` INTEGER, " +
                "`previewText` TEXT, " +
                "`lastUpdated` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))"
            )
            
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_BookmarkEntity_contentId_bookmarkType` " +
                "ON `BookmarkEntity`(`contentId`, `bookmarkType`)"
            )
        }
    }
}

