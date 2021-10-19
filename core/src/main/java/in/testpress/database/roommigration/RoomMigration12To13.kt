package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration12To13 {
    val MIGRATION_12_13: Migration = object : Migration(12, 13) {
        override fun migrate(database: SupportSQLiteDatabase) {
            val sql = "CREATE TABLE IF NOT EXISTS `DiscussionAnswerEntity` (`id` INTEGER, `forumThreadId` INTEGER, `approved_by_id` INTEGER, `approved_by_url` TEXT, `approved_by_username` TEXT, `approved_by_firstName` TEXT, `approved_by_lastName` TEXT, `approved_by_displayName` TEXT, `approved_by_photo` TEXT, `approved_by_largeImage` TEXT, `approved_by_mediumImage` TEXT, `approved_by_mediumSmallImage` TEXT, `approved_by_smallImage` TEXT, `approved_by_xSmallImage` TEXT, `approved_by_miniImage` TEXT, `comment_id` INTEGER, `comment_url` TEXT, `comment_userEmail` TEXT, `comment_userUrl` TEXT, `comment_comment` TEXT, `comment_submitDate` TEXT, `comment_upvotes` INTEGER, `comment_downvotes` INTEGER, `comment_typeOfVote` INTEGER, `comment_voteId` INTEGER, `comment_created` TEXT, `comment_modified` TEXT, `comment_contentId` INTEGER, `comment_contentUrl` TEXT, `comment_profileId` INTEGER, `comment_profileUrl` TEXT, `comment_username` TEXT, `comment_displayName` TEXT, `comment_firstName` TEXT, `comment_lastName` TEXT, `comment_email` TEXT, `comment_photo` TEXT, `comment_largeImage` TEXT, `comment_mediumImage` TEXT, `comment_smallImage` TEXT, `comment_xSmallImage` TEXT, `comment_miniImage` TEXT, `comment_birthDate` TEXT, `comment_gender` TEXT, `comment_address1` TEXT, `comment_address2` TEXT, `comment_city` TEXT, `comment_zip` TEXT, `comment_state` TEXT, `comment_stateChoices` TEXT, `comment_phone` TEXT, PRIMARY KEY(`id`))"
            database.execSQL(sql)
            database.execSQL("ALTER TABLE `CommentEntity` ADD COLUMN `created` TEXT");
            database.execSQL("ALTER TABLE `CommentEntity` ADD COLUMN `modified` TEXT");
        }
    }
}