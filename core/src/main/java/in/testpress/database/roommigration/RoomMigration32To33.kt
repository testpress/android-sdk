package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration32To33 {
    val MIGRATION_32_33: Migration = object : Migration(32, 33) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `ProductEntity` (`id` INTEGER NOT NULL, `url` TEXT, `title` TEXT, `slug` TEXT, `images` TEXT, `startDate` TEXT, `endDate` TEXT, `description` TEXT, `paymentLink` TEXT, `descriptionHtml` TEXT, `shortDescription` TEXT, `contentsCount` INTEGER NOT NULL, `chaptersCount` INTEGER NOT NULL, `videosCount` INTEGER NOT NULL, `attachmentsCount` INTEGER NOT NULL, `examsCount` INTEGER NOT NULL, `quizCount` INTEGER NOT NULL, `htmlCount` INTEGER NOT NULL, `videoConferenceCount` INTEGER NOT NULL, `livestreamCount` INTEGER NOT NULL, `price` TEXT, `strikeThroughPrice` TEXT, `institute` TEXT, `requiresShipping` INTEGER, `buyNowText` TEXT, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `PriceEntity` (`id` INTEGER NOT NULL, `productId` INTEGER NOT NULL, `name` TEXT, `price` TEXT NOT NULL, `validity` TEXT, `startDate` TEXT, `endDate` TEXT, PRIMARY KEY(`id`))")
        }
    }
}