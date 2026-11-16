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
    fun getSafeAnswerAt_sortsNullOrderLastWithoutShiftingValidItems() {
        val answerWithNullOrder = DomainUserSelectedAnswer(id = 1L, order = null)
        val answer0 = DomainUserSelectedAnswer(id = 2L, order = 0)
        val answer1 = DomainUserSelectedAnswer(id = 3L, order = 1)

        val list = listOf(answerWithNullOrder, answer1, answer0)

        assertEquals(answer0, list.getSafeAnswerAt(0))
        assertEquals(answer1, list.getSafeAnswerAt(1))
        assertEquals(answerWithNullOrder, list.getSafeAnswerAt(2))
    }

    @Test
    fun getSafeAnswerAt_returnsCorrectElementForSingleItemList() {
        val answer = DomainUserSelectedAnswer(id = 1L, order = 0)
        val list = listOf(answer)

        assertEquals(answer, list.getSafeAnswerAt(0))
        assertNull(list.getSafeAnswerAt(1))
        assertNull(list.getSafeAnswerAt(-1))
    }

    @Test
    fun getResolvedExplanation_prefersAuthoritativeExplanationHtml() {
        val answer = DomainUserSelectedAnswer(
            explanationHtml = "Answer explanation",
            question = DomainQuestion(explanation = "Question explanation")
        )
        assertEquals("Answer explanation", answer.getResolvedExplanation())
    }

    @Test
    fun getResolvedExplanation_fallsBackToQuestionExplanationWhenBlankOrNull() {
        val answerWithNull = DomainUserSelectedAnswer(
            explanationHtml = null,
            question = DomainQuestion(explanation = "Question explanation")
        )
        assertEquals("Question explanation", answerWithNull.getResolvedExplanation())

        val answerWithBlank = DomainUserSelectedAnswer(
            explanationHtml = "   ",
            question = DomainQuestion(explanation = "Question explanation")
        )
        assertEquals("Question explanation", answerWithBlank.getResolvedExplanation())
    }

    @Test
    fun getResolvedExplanation_returnsNullWhenBothAreNullOrBlank() {
        val answer = DomainUserSelectedAnswer(
            explanationHtml = " ",
            question = DomainQuestion(explanation = "  ")
        )
        assertNull(answer.getResolvedExplanation())
    }
}
