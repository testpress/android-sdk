package `in`.testpress.database

import `in`.testpress.database.roommigration.RoomMigration4To5
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(version = 6,
        entities = [
            ContentEntity::class,
            OfflineVideo::class,
            ProductEntity::class,
            PriceEntity::class,
            CourseEntity::class,
            ProductCourseEntity::class,
            ProductPriceEntity::class
])
abstract class TestpressDatabase: RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun offlineVideoDao(): OfflineVideoDao
    abstract fun productDao(): ProductDao

    companion object {
        private lateinit var INSTANCE: TestpressDatabase

        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(RoomMigration4To5.getCreateProductEntity())
                database.execSQL(RoomMigration4To5.getCreateCourseEntity())
                database.execSQL(RoomMigration4To5.getCreateProductCourseEntity())
            }
        }

        private val MIGRATION_5_6: Migration = object : Migration(5,6) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("ALTER TABLE CourseEntity ADD COLUMN attachmentsCount INTEGER")
                database.execSQL("ALTER TABLE CourseEntity ADD COLUMN slug TEXT")
                database.execSQL("ALTER TABLE CourseEntity ADD COLUMN htmlContentsCount INTEGER")
                database.execSQL("ALTER TABLE CourseEntity ADD COLUMN `order` INTEGER")
                database.execSQL("ALTER TABLE CourseEntity ADD COLUMN externalLinkLabel TEXT")

                database.execSQL("CREATE TABLE IF NOT EXISTS PriceEntity(id INTEGER, name TEXT, price TEXT, validity INTEGER, endDate TEXT, startDate TEXT, PRIMARY KEY(id))")

                database.execSQL("CREATE TABLE IF NOT EXISTS ProductPriceEntity(priceId INTEGER NOT NULL DEFAULT '', productId INTEGER NOT NULL DEFAULT '', PRIMARY KEY(productId,priceId))")
            }
        }

        operator fun invoke(context: Context): TestpressDatabase {
            synchronized(TestpressDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            TestpressDatabase::class.java, "testpress-database")
                            .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                            .build()
                }
            }
            return INSTANCE
        }
    }
}