package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.OfflineAttemptSection
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update

@Dao
interface OfflineAttemptSectionDao: BaseDao<OfflineAttemptSection> {
    @Query("SELECT * FROM OfflineAttemptSection WHERE sectionId = :sectionId")
    suspend fun getBySectionId(sectionId: Long?): OfflineAttemptSection?

    @Query("SELECT * FROM OfflineAttemptSection WHERE attemptId = :attemptId")
    suspend fun getByAttemptId(attemptId: Long): List<OfflineAttemptSection>

    @Query("SELECT * FROM OfflineAttemptSection WHERE attemptSectionId = :attemptSectionId")
    suspend fun getByAttemptSectionId(attemptSectionId: Long?): OfflineAttemptSection?

    @Query("SELECT * FROM OfflineAttemptSection WHERE attemptId = :attemptId AND id = :id")
    suspend fun getByAttemptIdAndId(attemptId: Long, id: Long): OfflineAttemptSection?

    @Update
    suspend fun update(offlineAttemptSection: OfflineAttemptSection)

    @Query("SELECT * FROM OfflineAttemptSection WHERE attemptId = :attemptId AND state IN (:states)")
    suspend fun getOfflineAttemptSectionsByAttemptIdAndStates(attemptId: Long, states: List<String>): List<OfflineAttemptSection>
}