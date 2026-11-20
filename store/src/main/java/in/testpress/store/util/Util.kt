package `in`.testpress.store.util

import android.text.Spannable
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import java.math.BigDecimal

fun generateRandom10CharString(): String {
    return (1..10)
        .map { ('a'..'z').toList() + ('0'..'9').toList() }
        .map { it.random() }
        .joinToString("")
}

fun parsePrice(price: String?): BigDecimal? {
    if (price.isNullOrBlank()) return null
    val trimmed = price.trim()
    val isNegative = trimmed.startsWith("-")
    val cleaned = trimmed.replace(Regex("[^0-9.,]"), "")
    if (cleaned.isEmpty()) return null

    val normalized = if (cleaned.contains('.') && cleaned.contains(',')) {
        val lastDot = cleaned.lastIndexOf('.')
        val lastComma = cleaned.lastIndexOf(',')
        if (lastDot > lastComma) {
            cleaned.replace(",", "")
        } else {
            cleaned.replace(".", "").replace(',', '.')
        }
    } else if (cleaned.contains(',')) {
        val commaIndex = cleaned.lastIndexOf(',')
        val digitsAfterComma = cleaned.length - commaIndex - 1
        if (digitsAfterComma != 3) {
            cleaned.replace(',', '.')
        } else {
            cleaned.replace(",", "")
        }
    } else {
        cleaned
    }

    val finalString = if (isNegative) "-$normalized" else normalized
    return finalString.toBigDecimalOrNull()
}

fun shouldDisplayStrikeThroughPrice(price: String?, strikeThroughPrice: String?): Boolean {
    val cleanPrice = parsePrice(price) ?: return false
    val cleanStrikePrice = parsePrice(strikeThroughPrice) ?: return false
    return cleanStrikePrice > BigDecimal.ZERO && cleanStrikePrice > cleanPrice
}

@JvmOverloads
fun formatPriceWithStrikethrough(
    price: String?,
    strikeThroughPrice: String?,
    currencyPrefix: String = "₹ "
): CharSequence {
    if (price.isNullOrBlank()) {
        return ""
    }

    val displayPrice = "$currencyPrefix${price.trim()}"
    if (!shouldDisplayStrikeThroughPrice(price, strikeThroughPrice)) {
        return displayPrice
    }

    val displayStrikePrice = "$currencyPrefix${strikeThroughPrice!!.trim()}"
    val fullText = "$displayPrice  $displayStrikePrice"
    val spannable = SpannableString(fullText)
    val startIndex = fullText.length - displayStrikePrice.length
    spannable.setSpan(
        StrikethroughSpan(),
        startIndex,
        fullText.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return spannable
}