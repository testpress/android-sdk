package `in`.testpress.exam.ui

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.models.greendao.*
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SaveAttemptSectionTest {

    private val attempt = Attempt()

    private val attemptSectionDao =
            TestpressSDKDatabase.getAttemptSectionDao(ApplicationProvider.getApplicationContext())


    @Before
    fun setup() {
        saveAttempt()
        saveAttemptSection()
    }

    @Test
    fun testSectionShouldSaveCorrectly() {
        val section = attemptSectionDao.queryBuilder().where(AttemptSectionDao.Properties.AttemptId.eq(1)).list()
        Assert.assertFalse(section.isEmpty())
    }

    private fun saveAttemptSection() {
        val attemptSection = listOf(AttemptSection(
                1,"state", "Url","Start", "End",
                "time", "name", "duration", 1,
                "instruction", 1))
        attemptSectionDao.insertOrReplaceInTx(attemptSection)
    }

    private fun saveAttempt() {
        val attemptDao = TestpressSDKDatabase.getAttemptDao(ApplicationProvider.getApplicationContext())
        attemptDao.insertOrReplace(Attempt(1))
    }
}
