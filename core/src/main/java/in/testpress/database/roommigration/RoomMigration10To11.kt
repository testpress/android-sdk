package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration10To11 {
    val MIGRATION_10_11: Migration = object : Migration(10, 11) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `DiscussionPostEntity` (`id` INTEGER, `shortWebUrl` TEXT, `shortUrl` TEXT, `webUrl` TEXT, `created` TEXT, `commentsUrl` TEXT, `url` TEXT, `modified` TEXT, `upvotes` INTEGER, `downvotes` INTEGER, `title` TEXT, `summary` TEXT, `isActive` INTEGER, `publishedDate` TEXT, `commentsCount` INTEGER, `isLocked` INTEGER, `subject` INTEGER, `viewsCount` INTEGER, `participantsCount` INTEGER, `lastCommentedTime` TEXT, `contentHtml` TEXT, `isPublic` INTEGER, `shortLink` TEXT, `institute` INTEGER, `slug` TEXT, `isPublished` INTEGER, `isApproved` INTEGER, `forum` INTEGER, `ipAddress` TEXT, `voteId` INTEGER, `typeOfVote` INTEGER, `published` INTEGER, `modifiedDate` INTEGER, `creatorId` INTEGER, `commentorId` INTEGER, `categoryId` INTEGER, `created_by_id` INTEGER, `created_by_url` TEXT, `created_by_username` TEXT, `created_by_firstName` TEXT, `created_by_lastName` TEXT, `created_by_displayName` TEXT, `created_by_photo` TEXT, `created_by_largeImage` TEXT, `created_by_mediumImage` TEXT, `created_by_mediumSmallImage` TEXT, `created_by_smallImage` TEXT, `created_by_xSmallImage` TEXT, `created_by_miniImage` TEXT, `last_commented_by_id` INTEGER, `last_commented_by_url` TEXT, `last_commented_by_username` TEXT, `last_commented_by_firstName` TEXT, `last_commented_by_lastName` TEXT, `last_commented_by_displayName` TEXT, `last_commented_by_photo` TEXT, `last_commented_by_largeImage` TEXT, `last_commented_by_mediumImage` TEXT, `last_commented_by_mediumSmallImage` TEXT, `last_commented_by_smallImage` TEXT, `last_commented_by_xSmallImage` TEXT, `last_commented_by_miniImage` TEXT, `category_id` INTEGER, `category_name` TEXT, `category_color` TEXT, `category_slug` TEXT, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `LastLoadedPageData` (`resourceType` TEXT NOT NULL, `previous` INTEGER, `next` INTEGER, PRIMARY KEY(`resourceType`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `UserEntity` (`id` INTEGER, `url` TEXT, `username` TEXT, `firstName` TEXT, `lastName` TEXT, `displayName` TEXT, `photo` TEXT, `largeImage` TEXT, `mediumImage` TEXT, `mediumSmallImage` TEXT, `smallImage` TEXT, `xSmallImage` TEXT, `miniImage` TEXT, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `CategoryEntity` (`id` INTEGER, `name` TEXT, `color` TEXT, `slug` TEXT, PRIMARY KEY(`id`))")
        }
    }
}