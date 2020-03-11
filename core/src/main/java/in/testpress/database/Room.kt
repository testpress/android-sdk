package `in`.testpress.database

import `in`.testpress.BuildConfig
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(version = 2, entities = [
    ContentEntity::class, AttachmentEntity::class, HtmlContentEntity::class,
    VideoContentEntity::class, ExamContentEntity::class, ContentAttemptEntity::class,
    AttemptEntity::class, LanguageEntity::class, StreamEntity::class
])
@TypeConverters(
    AttachementTypeConverter::class, ContentAttemptEntityTypeConverter::class,
    ExamContentEntityConverter::class, HtmlContentEntityConverter::class,
    VideoContentEntityConverter::class, ContentAttemptEntityTypeConverter::class,
    AttemptEntityTypeConverter::class, LanguageEntityTypeConverter::class,
    StreamEntityTypeConverter::class
)
abstract class TestpressDatabase: RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun attachmentDao(): AttachmentContentDao
    abstract fun htmlDao(): HtmlContentDao
    abstract fun videoDao(): VideoContentDao
    abstract fun examDao(): ExamContentDao
    abstract fun contentAttemptDao(): ContentAttemptDao
    abstract fun attemptDao(): AttemptDao
    abstract fun languageDao(): LanguageDao
    abstract fun streamDao(): StreamDao

    companion object {
        private lateinit var INSTANCE: TestpressDatabase

        operator fun invoke(context: Context): TestpressDatabase {
            synchronized(TestpressDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    val builder = Room.databaseBuilder(context.applicationContext,
                            TestpressDatabase::class.java, "testpress-db")
                    if (BuildConfig.DEBUG) {
                        builder.fallbackToDestructiveMigration()
                    }
                    INSTANCE = builder.build()
                }
            }
            return INSTANCE
        }
    }
}