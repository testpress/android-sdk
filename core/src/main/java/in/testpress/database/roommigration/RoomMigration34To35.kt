package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration34To35 {
    val MIGRATION_34_35: Migration = object : Migration(34, 35) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS CommentEntity")
            database.execSQL("DROP TABLE IF EXISTS DiscussionPostEntity")
            database.execSQL("DROP TABLE IF EXISTS LastLoadedPageData")
            database.execSQL("DROP TABLE IF EXISTS UserEntity")
            database.execSQL("DROP TABLE IF EXISTS CategoryEntity")
            database.execSQL("DROP TABLE IF EXISTS DiscussionThreadAnswerEntity")
        }
    }
}