package `in`.testpress.course.util

import android.app.Activity
import `in`.testpress.util.getRootDirPath
import org.junit.Assert.*
import org.junit.Test

class FileUtilsTest: Activity() {
    @Test
    fun test() {
        assertNotNull(getRootDirPath(this))
    }
}