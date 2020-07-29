package `in`.testpress.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(version = 2, entities = [ContentEntity::class, OfflineVideo::class])
abstract class TestpressDatabase: RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun offlineVideoDao(): OfflineVideoDao

    companion object {
        private lateinit var INSTANCE: TestpressDatabase

        operator fun invoke(context: Context): TestpressDatabase {
            synchronized(TestpressDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            TestpressDatabase::class.java, "testpress-db")
                        .fallbackToDestructiveMigration().build()
                }
            }
            return INSTANCE
        }
    }
}