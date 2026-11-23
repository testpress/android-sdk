package `in`.testpress.course.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DomainContentSortTest {

    private val comparator = compareBy<DomainContent>(
        { it.position == null },
        { it.position },
        { it.order },
        { it.id }
    )

    private fun createContent(id: Long, order: Int? = null, position: Int? = null) = DomainContent(
        id = id,
        order = order,
        position = position,
        title = "Content $id",
        isLocked = false,
        isScheduled = false,
        active = true,
        hasStarted = true,
        isCourseAvailable = true
    )

    @Test
    fun testSortOrderPositionedFirstNullsLastThenOrderThenId() {
        val item1 = createContent(id = 10, order = 5, position = 1)
        val item2 = createContent(id = 20, order = 1, position = 0)
        val item3 = createContent(id = 30, order = 2, position = null)
        val item4 = createContent(id = 40, order = 1, position = null)
        val item5 = createContent(id = 5, order = 1, position = null)

        val unsorted = listOf(item1, item3, item2, item5, item4)
        val sorted = unsorted.sortedWith(comparator)

        // Expected order:
        // 1. item2: position = 0
        // 2. item1: position = 1
        // 3. item5: position = null, order = 1, id = 5
        // 4. item4: position = null, order = 1, id = 40
        // 5. item3: position = null, order = 2, id = 30
        assertEquals(listOf(item2, item1, item5, item4, item3), sorted)
    }
}
