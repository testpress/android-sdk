package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.Direction
import androidx.room.Dao
import androidx.room.Query

@Dao
interface DirectionDao : BaseDao<Direction> {

    @Query("SELECT * FROM Direction WHERE id = :directionId")
    suspend fun getDirectionById(directionId: Long): Direction?
}