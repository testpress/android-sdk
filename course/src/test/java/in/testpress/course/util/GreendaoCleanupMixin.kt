package `in`.testpress.course.util

import `in`.testpress.core.TestpressSDKDatabase
import org.junit.After
import java.lang.reflect.Field

open class GreendaoCleanupMixin  {
    @After
    fun tearDown() {
        resetSingleton(TestpressSDKDatabase::class.java, "database")
        resetSingleton(TestpressSDKDatabase::class.java, "daoSession")
    }

    private fun resetSingleton(clazz: Class<TestpressSDKDatabase>, fieldName: String) {
        lateinit var instance: Field
        try {
            instance = clazz.getDeclaredField(fieldName)
            instance.isAccessible = true
            instance.set(null, null)
        } catch (e: Exception) {
            throw RuntimeException()
        }
    }

}