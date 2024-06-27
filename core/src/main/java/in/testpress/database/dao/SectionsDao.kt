package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.Section
import androidx.room.Dao
import androidx.room.Query

@Dao
interface SectionsDao : BaseDao<Section> {

    @Query("SELECT * FROM Section WHERE id IN (:sectionIds)")
    suspend fun getSectionsByIds(sectionIds: List<Long>): List<Section>

}