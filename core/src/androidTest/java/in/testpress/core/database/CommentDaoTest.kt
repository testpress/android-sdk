package `in`.testpress.core.database

import `in`.testpress.database.entities.CommentEntity
import `in`.testpress.database.entities.ContentObject
import `in`.testpress.database.entities.ProfileDetails
import `in`.testpress.util.getOrAwaitValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommentDaoTest: DbTestMixin() {

    @Test
    fun readShouldReturnInsertedData() {
        insertCommentIntoDb()

        val fetchedComment = db.commentDao().getAll().getOrAwaitValue()
        Assert.assertEquals(getComments(),fetchedComment)
    }

    private fun insertCommentIntoDb() {
        getComments().forEach {
            db.commentDao().insert(it)
        }
    }

    private fun getComments(): List<CommentEntity> {
        return listOf(CommentEntity(id = 1, contentObject = getContentObject(),
                user = getProfileDetails()))
    }

    private fun getContentObject(): ContentObject {
        return ContentObject(id = 1, url = "url")
    }

    private fun getProfileDetails(): ProfileDetails {
        return ProfileDetails(id = 1, displayName = "testPress",url = "url",username = "username",
                mediumImage = null, email = "email", miniImage = null,address2 = "address", address1 = null)
    }

}
