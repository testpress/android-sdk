package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.DiscussionAnswerEntity
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface DiscussionAnswerDao: BaseDao<DiscussionAnswerEntity> {
    @Query("SELECT * FROM discussionanswerentity")
    fun getAll(): List<DiscussionAnswerEntity>


    @Query("SELECT * FROM discussionanswerentity WHERE forumThreadId=:id")
    fun getByForumId(id: Long): LiveData<DiscussionAnswerEntity?>

}
