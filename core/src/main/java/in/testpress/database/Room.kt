package `in`.testpress.database

import `in`.testpress.database.dao.CommentDao
import `in`.testpress.database.entities.CommentEntity
import `in`.testpress.database.roommigration.RoomMigration4To5.MIGRATION_4_5
import `in`.testpress.database.roommigration.RoomMigration5To6.MIGRATION_5_6
import `in`.testpress.database.roommigration.RoomMigration3To4.MIGRATION_3_4
import `in`.testpress.database.roommigration.RoomMigration6To7.MIGRATION_6_7
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(version = 8,
        entities = [
            ContentEntity::class,
            OfflineVideo::class,
            ProductEntity::class,
            PriceEntity::class,
            CourseEntity::class,
            ProductCourseEntity::class,
            ProductPriceEntity::class,
            CommentEntity::class
        ], exportSchema = true)
abstract class TestpressDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun offlineVideoDao(): OfflineVideoDao
    abstract fun productDao(): ProductDao
    abstract fun commentDao(): CommentDao

    companion object {
        private lateinit var INSTANCE: TestpressDatabase

        val MIGRATIONS = arrayOf(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)

        operator fun invoke(context: Context): TestpressDatabase {
            synchronized(TestpressDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            TestpressDatabase::class.java, "testpress-database")
                            .addMigrations(*MIGRATIONS)
                            .build()
                }
            }
            return INSTANCE
        }
    }
}
