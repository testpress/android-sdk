package `in`.testpress.database

import `in`.testpress.database.dao.*
import `in`.testpress.database.entities.*
import `in`.testpress.database.roommigration.RoomMigration10To11.MIGRATION_10_11
import `in`.testpress.database.roommigration.RoomMigration11To12.MIGRATION_11_12
import `in`.testpress.database.roommigration.RoomMigration12To13.MIGRATION_12_13
import `in`.testpress.database.roommigration.RoomMigration13To14.MIGRATION_13_14
import `in`.testpress.database.roommigration.RoomMigration14To15.MIGRATION_14_15
import `in`.testpress.database.roommigration.RoomMigration15To16.MIGRATION_15_16
import `in`.testpress.database.roommigration.RoomMigration16To17.MIGRATION_16_17
import `in`.testpress.database.roommigration.RoomMigration4To5.MIGRATION_4_5
import `in`.testpress.database.roommigration.RoomMigration5To6.MIGRATION_5_6
import `in`.testpress.database.roommigration.RoomMigration3To4.MIGRATION_3_4
import `in`.testpress.database.roommigration.RoomMigration6To7.MIGRATION_6_7
import `in`.testpress.database.roommigration.RoomMigration7To8.MIGRATION_7_8
import `in`.testpress.database.roommigration.RoomMigration8To9.MIGRATION_8_9
import `in`.testpress.database.roommigration.RoomMigration9To10.MIGRATION_9_10
import `in`.testpress.util.Converters
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import `in`.testpress.database.roommigration.RoomMigration17To18.MIGRATION_17_18
import `in`.testpress.database.roommigration.RoomMigration18To19.MIGRATION_18_19
import `in`.testpress.database.roommigration.RoomMigration19To20.MIGRATION_19_20
import `in`.testpress.database.roommigration.RoomMigration20To21.MIGRATION_20_21
import `in`.testpress.database.roommigration.RoomMigration21To22.MIGRATION_21_22
import `in`.testpress.database.roommigration.RoomMigration22To23.MIGRATION_22_23

@Database(version = 23,
        entities = [
            ContentEntity::class,
            OfflineVideo::class,
            ProductEntity::class,
            PriceEntity::class,
            CourseEntity::class,
            ProductCourseEntity::class,
            ProductPriceEntity::class,
            CommentEntity::class,
            DiscussionPostEntity::class,
            LastLoadedPageData::class,
            UserEntity::class,
            CategoryEntity::class,
            DiscussionThreadAnswerEntity::class,
            ProductCategoryEntity::class,
            ContentEntityLite::class,
            ContentEntityLiteRemoteKey::class,
            UpcomingContentEntity::class,
            UpcomingContentRemoteKeys::class,
            Question::class,
            Subject::class,
            Direction::class,
            ExamQuestion::class,
            Language::class,
            Section::class,
            OfflineExam::class
        ], exportSchema = true)
@TypeConverters(Converters::class)
abstract class TestpressDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun offlineVideoDao(): OfflineVideoDao
    abstract fun productDao(): ProductDao
    abstract fun commentDao(): CommentDao
    abstract fun forumDao(): DiscussionPostDao
    abstract fun lastLoadedPageDataDao(): LastLoadedPageDataDao
    abstract fun categoryDao(): CategoryDao
    abstract fun discussionAnswerDao(): DiscussionAnswerDao
    abstract fun productCategoryDao(): ProductCategoryDao
    abstract fun contentLiteDao(): ContentLiteDao
    abstract fun contentLiteRemoteKeyDao():ContentLiteRemoteKeyDao
    abstract fun offlineExamDao():OfflineExamDao
    abstract fun languageDao():LanguageDao
    abstract fun directionDao():DirectionDao
    abstract fun subjectDao():SubjectDao
    abstract fun sectionsDao():SectionsDao
    abstract fun examQuestionDao():ExamQuestionDao
    abstract fun questionDao():QuestionDao

    companion object {
        private lateinit var INSTANCE: TestpressDatabase

        val MIGRATIONS = arrayOf(
            MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
            MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14,
            MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19,
            MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23
        )

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
