package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.OfflineAttempt
import `in`.testpress.database.entities.OfflineAttemptSection
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface OfflineAttemptSectionDao: BaseDao<OfflineAttemptSection> {
    @Query("SELECT * FROM OfflineAttemptSection WHERE attemptId = :attemptId")
    fun getByAttemptId(attemptId: Long): List<OfflineAttemptSection>
}