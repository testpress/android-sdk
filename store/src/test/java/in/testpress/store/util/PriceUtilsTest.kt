package `in`.testpress.store.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PriceUtilsTest {

    @Test
    fun shouldDisplayStrikeThroughPrice_whenStrikeThroughIsGreater_returnsTrue() {
        assertTrue(shouldDisplayStrikeThroughPrice("500", "1000"))
        assertTrue(shouldDisplayStrikeThroughPrice("499.99", "500.00"))
        assertTrue(shouldDisplayStrikeThroughPrice("₹ 999.00", "₹ 1,299.00"))
    }

    @Test
    fun shouldDisplayStrikeThroughPrice_whenPricesAreEqual_returnsFalse() {
        assertFalse(shouldDisplayStrikeThroughPrice("500", "500"))
        assertFalse(shouldDisplayStrikeThroughPrice("500.00", "500.00"))
        assertFalse(shouldDisplayStrikeThroughPrice("₹ 1,000", "₹ 1,000"))
    }

    @Test
    fun shouldDisplayStrikeThroughPrice_whenStrikeThroughIsLower_returnsFalse() {
        assertFalse(shouldDisplayStrikeThroughPrice("1000", "500"))
        assertFalse(shouldDisplayStrikeThroughPrice("500.00", "499.99"))
        assertFalse(shouldDisplayStrikeThroughPrice("₹ 1,299.00", "₹ 999.00"))
    }

    @Test
    fun shouldDisplayStrikeThroughPrice_whenStrikeThroughIsZeroOrNegative_returnsFalse() {
        assertFalse(shouldDisplayStrikeThroughPrice("500", "0"))
        assertFalse(shouldDisplayStrikeThroughPrice("500", "0.00"))
        assertFalse(shouldDisplayStrikeThroughPrice("500", "-100"))
        assertFalse(shouldDisplayStrikeThroughPrice("0", "0"))
    }

    @Test
    fun shouldDisplayStrikeThroughPrice_whenInputIsNullOrBlankOrNonNumeric_returnsFalse() {
        assertFalse(shouldDisplayStrikeThroughPrice(null, "1000"))
        assertFalse(shouldDisplayStrikeThroughPrice("500", null))
        assertFalse(shouldDisplayStrikeThroughPrice(null, null))
        assertFalse(shouldDisplayStrikeThroughPrice("", "1000"))
        assertFalse(shouldDisplayStrikeThroughPrice("500", ""))
        assertFalse(shouldDisplayStrikeThroughPrice("   ", "1000"))
        assertFalse(shouldDisplayStrikeThroughPrice("Free", "1000"))
        assertFalse(shouldDisplayStrikeThroughPrice("500", "Free"))
        assertFalse(shouldDisplayStrikeThroughPrice("invalid", "invalid"))
    }

    @Test
    fun shouldDisplayStrikeThroughPrice_withThousandsSeparators_handlesCorrectly() {
        assertTrue(shouldDisplayStrikeThroughPrice("1,000", "2,000"))
        assertTrue(shouldDisplayStrikeThroughPrice("₹ 999.00", "₹ 1,299.00"))
        assertTrue(shouldDisplayStrikeThroughPrice("99,999.00", "100,000.00"))
        assertFalse(shouldDisplayStrikeThroughPrice("1,000", "1,000"))
    }

    @Test
    fun shouldDisplayStrikeThroughPrice_withCommaDecimalSeparators_handlesCorrectly() {
        assertTrue(shouldDisplayStrikeThroughPrice("999,00", "1.299,00"))
        assertTrue(shouldDisplayStrikeThroughPrice("1299,00", "1299,50"))
        assertFalse(shouldDisplayStrikeThroughPrice("1.299,00", "1.299,00"))
        assertFalse(shouldDisplayStrikeThroughPrice("1.500,00", "1.299,00"))
    }

    @Test
    fun formatPriceWithStrikethrough_whenPriceIsBlank_returnsEmptyString() {
        assertEquals("", formatPriceWithStrikethrough(null, "1000"))
        assertEquals("", formatPriceWithStrikethrough("", "1000"))
        assertEquals("", formatPriceWithStrikethrough("   ", "1000"))
    }

    @Test
    fun formatPriceWithStrikethrough_whenNoStrikethroughNeeded_returnsPlainTextPrice() {
        assertEquals("₹ 500", formatPriceWithStrikethrough("500", null))
        assertEquals("₹ 500", formatPriceWithStrikethrough("500", "500"))
        assertEquals("₹ 500", formatPriceWithStrikethrough("500", "300"))
        assertEquals("500", formatPriceWithStrikethrough("500", null, currencyPrefix = ""))
        assertEquals("Rs. 500", formatPriceWithStrikethrough("500", "500", currencyPrefix = "Rs. "))
    }
}
