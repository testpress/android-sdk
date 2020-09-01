package `in`.testpress.database

import `in`.testpress.util.Converters
import android.content.Context
import androidx.room.*


@Database(version = 6,
        entities = [
            ContentEntity::class, OfflineVideo::class,
            ProductsListEntity::class
])
@TypeConverters(Converters::class)
abstract class TestpressDatabase: RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun offlineVideoDao(): OfflineVideoDao
    abstract fun productsListDao(): ProductsListDao

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