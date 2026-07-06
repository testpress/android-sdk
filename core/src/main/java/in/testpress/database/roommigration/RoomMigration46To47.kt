package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration46To47 {
    @JvmField
    val MIGRATION_46_47: Migration = object : Migration(46, 47) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE ContentEntity ADD COLUMN hasArtifacts INTEGER")
            database.execSQL("ALTER TABLE ContentEntity ADD COLUMN artifactsUrl TEXT")
        }
    }
}
