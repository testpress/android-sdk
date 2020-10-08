package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration5To6 {

    val MIGRATION_5_6: Migration = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS CourseEntity")
            database.execSQL(getCourseEntityTableCreationSQL())
            database.execSQL(getPriceEntityTableCreationSQL())
            database.execSQL(getProductPriceEntityTableCreationSQL())
            database.execSQL(getContentEntityTableCreationSQL())
        }

    }

    fun getCourseEntityTableCreationSQL(): String {
        return "CREATE TABLE IF NOT EXISTS CourseEntity(id INTEGER, image TEXT, examsCount INTEGER, created TEXT, description TEXT, title TEXT, chaptersCount INTEGER," +
                " deviceAccessControl TEXT, createdBy INTEGER, enableDiscussions INTEGER, url TEXT, contentsCount INTEGER, contentsUrl TEXT, chaptersUrl TEXT, modified TEXT," +
                " videosCount INTEGER, externalContentLink TEXT, attachmentsCount INTEGER, slug TEXT, htmlContentsCount INTEGER, `order` INTEGER, externalLinkLabel TEXT, PRIMARY KEY(id))"

    }

    fun getPriceEntityTableCreationSQL(): String {
        return "CREATE TABLE IF NOT EXISTS PriceEntity(id INTEGER, name TEXT, price TEXT, validity INTEGER, endDate TEXT, startDate TEXT, PRIMARY KEY(id))"
    }

    fun getProductPriceEntityTableCreationSQL(): String {
        return "CREATE TABLE IF NOT EXISTS ProductPriceEntity(priceId INTEGER NOT NULL DEFAULT '', productId INTEGER NOT NULL DEFAULT '', PRIMARY KEY(productId,priceId))"
    }

    fun getContentEntityTableCreationSQL(): String {
        return "CREATE TABLE IF NOT EXISTS ContentEntity(id INTEGER NOT NULL, title TEXT, description TEXT, image TEXT, `order` INTEGER, url TEXT NOT NULL, chapterId INTEGER, " +
                "chapterSlug TEXT NOT NULL, chapterUrl TEXT, courseId INTEGER, freePreview INTEGER, modified TEXT, contentType TEXT NOT NULL, examUrl TEXT, videoUrl TEXT, attachmentUrl TEXT," +
                " htmlUrl TEXT, isLocked INTEGER NOT NULL, isScheduled INTEGER NOT NULL, attemptsCount INTEGER NOT NULL, bookmarkId INTEGER, videoWatchedPercentage INTEGER, active INTEGER NOT NULL," +
                " examId INTEGER, attachmentId INTEGER, videoId INTEGER, htmlId INTEGER, start TEXT, hasStarted INTEGER NOT NULL, isCourseAvailable INTEGER, PRIMARY KEY(id))"

    }
}
