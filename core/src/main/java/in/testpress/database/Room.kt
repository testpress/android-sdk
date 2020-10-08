package `in`.testpress.database

import `in`.testpress.database.roommigration.RoomMigration4To5
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(version = 5,
        entities = [
            ContentEntity::class,
            OfflineVideo::class,
            ProductEntity::class,
            PriceEntity::class,
            CourseEntity::class,
            ProductCourseEntity::class,
            ProductPriceEntity::class
], exportSchema = true)
abstract class TestpressDatabase: RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun offlineVideoDao(): OfflineVideoDao
    abstract fun productDao(): ProductDao

    companion object {
        private lateinit var INSTANCE: TestpressDatabase

        private val MIGRATION_4_5 = RoomMigration4To5.MIGRATION_4_5

        operator fun invoke(context: Context): TestpressDatabase {
            synchronized(TestpressDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            TestpressDatabase::class.java, "testpress-database")
                            .addMigrations(MIGRATION_4_5)
                            .build()
                }
            }
            return INSTANCE
        }
    }
}