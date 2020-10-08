package `in`.testpress.database.roommigration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigration4To5 {

    val MIGRATION_4_5: Migration = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(getCreateProductEntity())
            database.execSQL(getCreateCourseEntity())
            database.execSQL(getCreateProductCourseEntity())
        }

    }

    private fun getCreateProductEntity(): String {
        return "CREATE TABLE IF NOT EXISTS " +
                "ProductEntity (image TEXT, endDate TEXT, furl TEXT,currentPrice TEXT," +
                "surl TEXT,descriptionHtml TEXT,id INTEGER,title TEXT,paymentLink TEXT," +
                "buyNowText TEXT, slug TEXT, startDate TEXT, PRIMARY KEY(id))"
    }

    private fun getCreateCourseEntity(): String {
        return  "CREATE TABLE IF NOT EXISTS " +
                "CourseEntity (id INTEGER,image TEXT,examsCount INTEGER,created TEXT,description TEXT,title TEXT," +
                "chaptersCount INTEGER,deviceAccessControl TEXT,createdBy INTEGER,enableDiscussions INTEGER," +
                "url TEXT, contentsCount INTEGER,contentsUrl TEXT,chaptersUrl TEXT,modified TEXT,videosCount INTEGER," +
                "externalContentLink TEXT, PRIMARY KEY(id))"
    }

    private fun getCreateProductCourseEntity(): String {
        return "CREATE TABLE IF NOT EXISTS ProductCourseEntity(courseId INTEGER NOT NULL DEFAULT '', " +
                "productId INTEGER NOT NULL DEFAULT '', PRIMARY KEY(productId,courseId))"
    }
}
