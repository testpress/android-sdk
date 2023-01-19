package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration14To15 {
    val MIGRATION_14_15: Migration = object : Migration(14, 15) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `ProductCategoryEntity` (`id` INTEGER, `name` TEXT, `slug` TEXT, PRIMARY KEY(`id`))");
        }
    }
}