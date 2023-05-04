package `in`.testpress.core.database

import `in`.testpress.database.ContentEntity
import `in`.testpress.util.getOrAwaitValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContentDaoTest : DbTestMixin() {
    fun createContent(): ContentEntity {
        val content = ContentEntity(
            id = 1,
            active = true,
            isLocked = false,
            isScheduled = false,
            hasStarted = true
        )
        content.title = "Content"
        content.order = 0
        content.contentType = "exam"
        return content
    }

    @Test
    fun readWrite() {
        val content = createContent()
        db.contentDao().insert(content)

        val fetchedContent = db.contentDao().findById(1).getOrAwaitValue()
        assertThat(fetchedContent, equalTo(content))
    }

    @Test
    fun delete() {
        val content = createContent()
        db.contentDao().insert(content)
        db.contentDao().delete(content)
        val fetchedContents = db.contentDao().getAll().getOrAwaitValue()

        assertTrue(fetchedContents.isEmpty())
    }

    @Test
    fun update() {
        val content = createContent()
        db.contentDao().insert(content)
        val fetchedContent = db.contentDao().findById(1).getOrAwaitValue()
        assertThat(fetchedContent.title, equalTo("Content"))

        content.title = "Updated Content"
        db.contentDao().update(content)
        val updatedContent = db.contentDao().findById(1).getOrAwaitValue()
        assertThat(updatedContent.title, equalTo("Updated Content"))
    }

    @Test
    fun getChapterContents() {
        val content = createContent()
        content.chapterId = 1
        db.contentDao().insert(content)
        val fetchedContents = db.contentDao().getChapterContents(1).getOrAwaitValue()

        assertEquals(listOf(content), fetchedContents)
    }
}
