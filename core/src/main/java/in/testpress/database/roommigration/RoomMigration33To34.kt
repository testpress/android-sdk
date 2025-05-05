package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration33To34 {
    val MIGRATION_33_34: Migration = object : Migration(33, 34) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS ProductLiteEntity")
            database.execSQL("CREATE TABLE IF NOT EXISTS `ProductLiteEntity` (`id` INTEGER NOT NULL, `title` TEXT NOT NULL, `slug` TEXT NOT NULL, `images` TEXT, `courseIds` TEXT, `categoryId` INTEGER, `contentsCount` INTEGER NOT NULL, `chaptersCount` INTEGER NOT NULL, `order` INTEGER NOT NULL, `price` TEXT NOT NULL, PRIMARY KEY(`id`))")
        }
    }
}