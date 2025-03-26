package `in`.testpress.store.data.database

import android.content.Context
import androidx.room.*
import `in`.testpress.store.data.database.model.ProductCategoryEntity
import `in`.testpress.store.data.database.model.ProductLiteEntity

@Database(version = 1,
    entities = [
        ProductLiteEntity::class,
        ProductCategoryEntity::class
    ], exportSchema = true)
@TypeConverters(Converters::class)
abstract class TestpressStoreDatabase : RoomDatabase() {

    companion object {
        private lateinit var INSTANCE: TestpressStoreDatabase

        operator fun invoke(context: Context): TestpressStoreDatabase {
            synchronized(TestpressStoreDatabase::class.java) {
                if (!Companion::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        TestpressStoreDatabase::class.java, "testpress-store-database")
                        .build()
                }
            }
            return INSTANCE
        }
    }
}
