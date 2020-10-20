package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration6To7 {
    val MIGRATION_6_7: Migration = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(getCommentEntityTableCreationSQL())
        }
    }

    private fun getCommentEntityTableCreationSQL(): String {
        return "CREATE TABLE IF NOT EXISTS `CommentEntity` (`id` INTEGER, `url` TEXT, `userEmail` TEXT, `userUrl` TEXT, `comment` TEXT, `submitDate` TEXT," +
                " `upvotes` INTEGER, `downvotes` INTEGER, `typeOfVote` INTEGER, `voteId` INTEGER, `contentId` INTEGER, `contentUrl` TEXT, " +
                "`profileId` INTEGER, `profileUrl` TEXT, `username` TEXT, `displayName` TEXT, `firstName` TEXT, `lastName` TEXT, `email` TEXT, " +
                "`photo` TEXT, `largeImage` TEXT, `mediumImage` TEXT, `smallImage` TEXT, `xSmallImage` TEXT, `miniImage` TEXT, `birthDate` TEXT, `gender` TEXT, " +
                "`address1` TEXT, `address2` TEXT, `city` TEXT, `zip` TEXT, `state` TEXT, `stateChoices` TEXT, `phone` TEXT, PRIMARY KEY(`id`))"
    }
}
