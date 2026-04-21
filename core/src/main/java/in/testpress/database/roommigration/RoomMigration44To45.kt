package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration44To45 {
    @JvmField
    val MIGRATION_44_45: Migration = object : Migration(44, 45) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE OfflineExam ADD COLUMN enableMindsetReflections INTEGER")
            database.execSQL("ALTER TABLE OfflineExam ADD COLUMN preExamReflectionForm TEXT")
        }
    }
}
