package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration43To44 {
    @JvmField
    val MIGRATION_43_44: Migration = object : Migration(43, 44) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE ContentEntity ADD COLUMN enableTranscript INTEGER")
            database.execSQL("ALTER TABLE ContentEntity ADD COLUMN videoSubtitleUrl TEXT")
            database.execSQL("ALTER TABLE ContentEntity ADD COLUMN videoSubtitleLanguage TEXT")
            database.execSQL("ALTER TABLE ContentEntity ADD COLUMN videoSubtitleJobStatus TEXT")
        }
    }
}

