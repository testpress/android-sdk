package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.DiscussionThreadAnswerEntity
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface DiscussionAnswerDao: BaseDao<DiscussionThreadAnswerEntity> {
    @Query("SELECT * FROM DiscussionThreadAnswerEntity")
    fun getAll(): List<DiscussionThreadAnswerEntity>


    @Query("SELECT * FROM DiscussionThreadAnswerEntity WHERE forumThreadId=:id")
    fun getByForumId(id: Long): LiveData<DiscussionThreadAnswerEntity?>

}
