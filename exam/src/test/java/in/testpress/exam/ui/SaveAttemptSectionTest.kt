package `in`.testpress.exam.ui

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.models.greendao.*
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SaveAttemptSectionTest {

    private val attempt = Attempt()

    private val courseAttempt = CourseAttempt()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val attemptSectionDao =
            TestpressSDKDatabase.getAttemptSectionDao(context)

    @Test
    fun saveInDBShouldSaveAttemptSection() {
        saveAttempt()
        saveAttemptSection()

        courseAttempt.saveInDB(context, Content(1))

        val section = attemptSectionDao.queryBuilder().where(AttemptSectionDao.Properties.AttemptId.eq(1)).list()
        Assert.assertFalse(section.isEmpty())
    }

    private fun saveAttemptSection() {
        val attemptSection = listOf(AttemptSection(
                1,"state", "https://sandox.testpress.in","Start", "End",
                "time", "name", "duration", 1,
                "instruction", 1))
        attemptSectionDao.insertOrReplaceInTx(attemptSection)
    }

    private fun saveAttempt() {
        val attemptDao = TestpressSDKDatabase.getAttemptDao(ApplicationProvider.getApplicationContext())
        attemptDao.insertOrReplace(Attempt(
                "https://sandox.testpress.in",1,"date",10,"10","2","10",
                "reviewUrl","Question url",2,1,"","","","","",1,1,"per"
        ))
    }
}
