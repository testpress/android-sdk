package `in`.testpress.course.util

import `in`.testpress.course.util.SHA256Generator.generateSha256
import org.junit.Assert.*
import org.junit.Test

class SHA256GeneratorTest {

    @Test
    fun shaShouldBeSameForSameString() {
        assertEquals("testpress".generateSha256(), "testpress".generateSha256())
    }

    @Test
    fun shaShouldBeDifferentForDifferentString() {
        assertNotNull("testPress".generateSha256(), "Testpress".generateSha256())
    }

    @Test
    fun testShaShouldNotReturnNull() {
        assertNotNull("Hello".generateSha256())
    }

    @Test
    fun shaShouldNotBeSameForStringWithVariationInCase() {
        assertNotEquals("testpress".generateSha256(), "TestPress".generateSha256())
    }
}
