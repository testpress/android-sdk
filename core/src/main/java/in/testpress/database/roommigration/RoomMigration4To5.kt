package `in`.testpress.database.roommigration

object RoomMigration4To5 {

    fun getCreateProductEntity(): String {
        return "CREATE TABLE " +
                "ProductEntity (image TEXT, endDate TEXT, furl TEXT,currentPrice TEXT," +
                "surl TEXT,descriptionHtml TEXT,id INTEGER,title TEXT,paymentLink TEXT," +
                "buyNowText TEXT, slug TEXT, startDate TEXT, PRIMARY KEY(id))"
    }

    fun getCreateCourseEntity(): String {
        return  "CREATE TABLE " +
                "CourseEntity (id INTEGER,image TEXT,examsCount INTEGER,created TEXT,description TEXT,title TEXT," +
                "chaptersCount INTEGER,deviceAccessControl TEXT,createdBy INTEGER,enableDiscussions INTEGER," +
                "url TEXT, contentsCount INTEGER,contentsUrl TEXT,chaptersUrl TEXT,modified TEXT,videosCount INTEGER," +
                "externalContentLink TEXT, PRIMARY KEY(id))"
    }

    fun getCreateProductCourseEntity(): String {
        return "CREATE TABLE ProductCourseEntity(courseId INTEGER NOT NULL DEFAULT '', " +
                "productId INTEGER NOT NULL DEFAULT '', PRIMARY KEY(productId,courseId))"
    }
}