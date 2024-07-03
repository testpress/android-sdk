package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.OfflineAttempt
import `in`.testpress.database.entities.OfflineAttemptSection
import androidx.room.Dao
import androidx.room.Query

@Dao
interface OfflineAttemptSectionDao: BaseDao<OfflineAttemptSection> {
    @Query("SELECT * FROM OfflineAttemptSection WHERE sectionId = :sectionId")
    suspend fun getById(sectionId: Long?): OfflineAttemptSection?
}