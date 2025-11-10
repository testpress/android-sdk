package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration39To40 {
    val MIGRATION_39_40: Migration = object : Migration(39, 40) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `VideoQuestion` " +
                "(`videoContentId` INTEGER NOT NULL, " +
                "`id` INTEGER NOT NULL, " +
                "`position` INTEGER NOT NULL, " +
                "`order` INTEGER NOT NULL, " +
                "`questionId` INTEGER NOT NULL, " +
                "`questionType` TEXT NOT NULL, " +
                "`questionHtml` TEXT NOT NULL, " +
                "PRIMARY KEY(`videoContentId`, `id`))"
            )
            
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `VideoAnswer` " +
                "(`videoContentId` INTEGER NOT NULL, " +
                "`videoQuestionId` INTEGER NOT NULL, " +
                "`id` INTEGER NOT NULL, " +
                "`isCorrect` INTEGER NOT NULL, " +
                "`textHtml` TEXT NOT NULL, " +
                "PRIMARY KEY(`videoContentId`, `videoQuestionId`, `id`), " +
                "FOREIGN KEY(`videoContentId`, `videoQuestionId`) " +
                "REFERENCES `VideoQuestion`(`videoContentId`, `id`) " +
                "ON DELETE CASCADE)"
            )
            
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_VideoAnswer_videoContentId_videoQuestionId` " +
                "ON `VideoAnswer`(`videoContentId`, `videoQuestionId`)"
            )
        }
    }
}

