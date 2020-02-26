package `in`.testpress.course

import `in`.testpress.course.db.Content
import `in`.testpress.course.db.ContentDao
import `in`.testpress.course.db.TestpressDatabase
import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class TestpressDatabaseTest {
    private lateinit var contentDao: ContentDao
    private lateinit var db: TestpressDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getContext()
        db = Room.inMemoryDatabaseBuilder(context, TestpressDatabase::class.java).build()
        contentDao = db.contentDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    fun createContent(): Content {
        return Content(
                id = 1, title = "Content", active = true,
                order = 0, contentType = "exam", isLocked = false,
                isScheduled = false
        )
    }

    @Test
    fun readWrite() {
        val content = createContent()
        contentDao.insert(content)
        val fetchedContent = contentDao.findById(1)

        assertThat(fetchedContent, equalTo(content))
    }

    @Test
    fun delete() {
        val content = createContent()
        contentDao.insert(content)
        contentDao.delete(content)
        val fetchedContents = contentDao.getAll().getAbsoluteValue()

        assertTrue(fetchedContents.isEmpty())
    }

    @Test
    fun update() {
        val content = createContent()
        contentDao.insert(content)
        val fetchedContent = contentDao.findById(1)
        assertThat(fetchedContent.title, equalTo("Content"))

        content.title = "Updated Content"
        contentDao.update(content)
        val updatedContent = contentDao.findById(1)
        assertThat(updatedContent.title, equalTo("Updated Content"))
    }
}