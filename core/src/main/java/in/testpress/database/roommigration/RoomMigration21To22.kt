package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration21To22 {
    val MIGRATION_21_22: Migration = object : Migration(21, 22) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS ExamQuestion")
            database.execSQL("DROP TABLE IF EXISTS Question")
            database.execSQL("DROP TABLE IF EXISTS Answer")
            database.execSQL("CREATE TABLE IF NOT EXISTS `ExamQuestion` (`id` INTEGER, `order` INTEGER, `questionId` INTEGER, `sectionId` INTEGER, `marks` TEXT, `partialMarks` TEXT, `examId` INTEGER, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `Question` (`id` INTEGER, `questionHtml` TEXT, `directionId` INTEGER, `answers` TEXT NOT NULL, `language` TEXT, `subjectId` INTEGER, `type` TEXT, `translations` TEXT NOT NULL, `marks` TEXT, `negativeMarks` TEXT, `parentId` INTEGER, PRIMARY KEY(`id`))")
        }
    }
}
