package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration42To43 {
    @JvmField
    val MIGRATION_42_43: Migration = object : Migration(42, 43) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE ContentEntity ADD COLUMN canEnableLearnLensAI INTEGER")
            database.execSQL("ALTER TABLE ContentEntity ADD COLUMN aiNotesUrl TEXT")
            database.execSQL("ALTER TABLE ContentEntity ADD COLUMN learnlensAssetStatus TEXT")
        }
    }
}
