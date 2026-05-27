package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration45To46 {
    @JvmField
    val MIGRATION_45_46: Migration = object : Migration(45, 46) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE PriceEntity ADD COLUMN purchaseValidityType INTEGER")
            database.execSQL("ALTER TABLE PriceEntity ADD COLUMN absoluteExpiryDate TEXT")
        }
    }
}
