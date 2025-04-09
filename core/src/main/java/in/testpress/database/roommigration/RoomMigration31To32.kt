package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration31To32 {
    val MIGRATION_31_32: Migration = object : Migration(31, 32) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS ProductEntity")
            database.execSQL("DROP TABLE IF EXISTS PriceEntity")
            database.execSQL("DROP TABLE IF EXISTS CourseEntity")
            database.execSQL("DROP TABLE IF EXISTS ProductCourseEntity")
            database.execSQL("DROP TABLE IF EXISTS ProductPriceEntity")
        }
    }
}