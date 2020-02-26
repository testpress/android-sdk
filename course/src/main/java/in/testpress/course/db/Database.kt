package `in`.testpress.course.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context


@Database(entities = [Content::class], version = 1)
abstract class TestpressDatabase: RoomDatabase() {
    abstract fun contentDao(): ContentDao

    companion object {
        private var instance: TestpressDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
                TestpressDatabase::class.java, "testpress.db")
                .build()

        @JvmStatic fun getDatabase(context: Context): TestpressDatabase? {
            if (instance == null) {
                synchronized(TestpressDatabase::class) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            TestpressDatabase::class.java, "testpress.db"
                    ).build()
                }
            }
            return instance
        }
    }
}