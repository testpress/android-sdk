package `in`.testpress.database

import android.content.Context
import androidx.room.*

@Database(version = 5,
        entities = [
            ContentEntity::class,
            OfflineVideo::class,
            ProductEntity::class,
            CourseEntity::class,
            ProductCourseEntity::class
])
abstract class TestpressDatabase: RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun offlineVideoDao(): OfflineVideoDao
    abstract fun productDao(): ProductDao

    companion object {
        private lateinit var INSTANCE: TestpressDatabase

        operator fun invoke(context: Context): TestpressDatabase {
            synchronized(TestpressDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            TestpressDatabase::class.java, "testpress-database").build()
                }
            }
            return INSTANCE
        }
    }
}