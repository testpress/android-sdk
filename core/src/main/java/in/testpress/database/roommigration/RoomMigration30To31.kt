package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration30To31 {
    val MIGRATION_30_31: Migration = object : Migration(30, 31) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `ProductLiteEntity` (`id` INTEGER NOT NULL, `title` TEXT NOT NULL, `slug` TEXT NOT NULL, `images` TEXT, `categoryId` INTEGER, `contentsCount` INTEGER NOT NULL, `chaptersCount` INTEGER NOT NULL, `order` INTEGER NOT NULL, `price` TEXT NOT NULL, PRIMARY KEY(`id`))")
        }
    }
}