package `in`.testpress.database

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
                database.execSQL(
                        "CREATE TABLE " +
                                "ProductEntity (image TEXT, endDate TEXT, furl TEXT,currentPrice TEXT," +
                                "surl TEXT,descriptionHtml TEXT,id INTEGER,title TEXT,paymentLink TEXT," +
                                "buyNowText TEXT, slug TEXT, startDate TEXT, PRIMARY KEY(id))" +
                                "")

                database.execSQL(
                        "CREATE TABLE " +
                                "CourseEntity (id INTEGER,image TEXT,examsCount INTEGER,created TEXT,description TEXT,title TEXT," +
                                "chaptersCount INTEGER,deviceAccessControl TEXT,createdBy INTEGER,enableDiscussions INTEGER," +
                                "url TEXT, contentsCount INTEGER,contentsUrl TEXT,chaptersUrl TEXT,modified TEXT,videosCount INTEGER," +
                                "externalContentLink TEXT, PRIMARY KEY(id))")

                database.execSQL(
                        "CREATE TABLE ProductCourseEntity(courseId INTEGER NOT NULL DEFAULT '', productId INTEGER NOT NULL DEFAULT '', PRIMARY KEY(productId,courseId))")

            }
        }

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