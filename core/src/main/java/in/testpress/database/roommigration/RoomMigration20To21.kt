package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration20To21 {
    val MIGRATION_20_21: Migration = object : Migration(20, 21) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS Language")
            database.execSQL("CREATE TABLE IF NOT EXISTS `Language` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `code` TEXT, `title` TEXT, `examId` INTEGER)")
        }
    }
}
