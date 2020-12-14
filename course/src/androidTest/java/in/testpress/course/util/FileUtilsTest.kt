package `in`.testpress.course.util

import android.app.Activity
import org.junit.Assert.*
import org.junit.Test

class FileUtilsTest: Activity() {
    @Test
    fun test() {
        assertNotNull(FileUtils.getRootDirPath(this))
    }
}