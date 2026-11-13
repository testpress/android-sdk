package `in`.testpress.exam.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DomainUserSelectedAnswerTest {

    @Test
    fun getSafeAnswerAt_returnsNullForNullList() {
        val list: List<DomainUserSelectedAnswer>? = null
        assertNull(list.getSafeAnswerAt(0))
        assertNull(list.getSafeAnswerAt(-1))
    }

    @Test
    fun getSafeAnswerAt_returnsNullForEmptyList() {
        val list = emptyList<DomainUserSelectedAnswer>()
        assertNull(list.getSafeAnswerAt(0))
        assertNull(list.getSafeAnswerAt(-1))
    }

    @Test
    fun getSafeAnswerAt_returnsNullForOutOfRangeIndex() {
        val list = listOf(
            DomainUserSelectedAnswer(id = 1L, order = 0),
            DomainUserSelectedAnswer(id = 2L, order = 1)
        )
        assertNull(list.getSafeAnswerAt(-1))
        assertNull(list.getSafeAnswerAt(2))
        assertNull(list.getSafeAnswerAt(100))
    }

    @Test
    fun getSafeAnswerAt_returnsAnswersSortedByOrder() {
        val answer0 = DomainUserSelectedAnswer(id = 10L, order = 0)
        val answer1 = DomainUserSelectedAnswer(id = 20L, order = 1)
        val answer2 = DomainUserSelectedAnswer(id = 30L, order = 2)

        val list = listOf(answer2, answer0, answer1)

        assertEquals(answer0, list.getSafeAnswerAt(0))
        assertEquals(answer1, list.getSafeAnswerAt(1))
        assertEquals(answer2, list.getSafeAnswerAt(2))
    }

    @Test
    fun getSafeAnswerAt_handlesNullOrderGracefully() {
        val answerWithNullOrder = DomainUserSelectedAnswer(id = 1L, order = null)
        val answerWithOrder1 = DomainUserSelectedAnswer(id = 2L, order = 1)

        val list = listOf(answerWithOrder1, answerWithNullOrder)

        assertEquals(answerWithNullOrder, list.getSafeAnswerAt(0))
        assertEquals(answerWithOrder1, list.getSafeAnswerAt(1))
    }
}
