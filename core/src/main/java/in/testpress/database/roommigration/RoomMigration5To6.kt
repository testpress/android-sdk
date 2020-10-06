package `in`.testpress.database.roommigration

object RoomMigration5To6 {

    fun getCreateProductEntity(): String {
        return "CREATE TABLE IF NOT EXISTS `ContentEntity` (`id` INTEGER NOT NULL, `title` TEXT, " +
                "`description` TEXT, `image` TEXT, `order` INTEGER, `url` TEXT NOT NULL, `chapterId` INTEGER, `chapterSlug` TEXT NOT NULL, " +
                "`chapterUrl` TEXT, `courseId` INTEGER, `freePreview` INTEGER, `modified` TEXT, `contentType` TEXT NOT NULL, `examUrl` TEXT," +
                " `videoUrl` TEXT, `attachmentUrl` TEXT, `htmlUrl` TEXT, `isLocked` INTEGER NOT NULL, `isScheduled` INTEGER NOT NULL, " +
                "`attemptsCount` INTEGER NOT NULL, `bookmarkId` INTEGER, `videoWatchedPercentage` INTEGER, `active` INTEGER NOT NULL, `examId` INTEGER, " +
                "`attachmentId` INTEGER,`videoId` INTEGER, `htmlId` INTEGER, `start` TEXT, `hasStarted` INTEGER NOT NULL, `isCourseAvailable` INTEGER, PRIMARY KEY(`id`))"
    }

    fun getCreateCourseEntity(): String {
        return "CREATE TABLE IF NOT EXISTS `OfflineVideo` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `title` TEXT, `description` TEXT," +
                " `remoteThumbnail` TEXT, `localThumbnail` TEXT, `duration` TEXT NOT NULL, `url` TEXT, `contentId` INTEGER, `percentageDownloaded` INTEGER NOT NULL," +
                " `bytesDownloaded` INTEGER NOT NULL, `totalSize` INTEGER NOT NULL, `courseId` INTEGER)"

    }

    fun getCreateProductCourseEntity(): String {
        return "CREATE TABLE IF NOT EXISTS ProductCourseEntity (courseId INTEGER NOT NULL, productId INTEGER NOT NULL, PRIMARY KEY(productId, courseId))"
    }

    fun getCreatePriceEntity(): String {
        return "CREATE TABLE IF NOT EXISTS PriceEntity(id INTEGER, name TEXT, price TEXT, validity INTEGER, endDate TEXT, startDate TEXT, PRIMARY KEY(id))"
    }

    fun getCreateProductPriceEntity(): String {
        return "CREATE TABLE IF NOT EXISTS ProductPriceEntity(priceId INTEGER NOT NULL DEFAULT '', productId INTEGER NOT NULL DEFAULT '', PRIMARY KEY(productId,priceId))"
    }
}