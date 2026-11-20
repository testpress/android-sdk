package `in`.testpress.store.util

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan

fun generateRandom10CharString(): String {
    return (1..10)
        .map { ('a'..'z').toList() + ('0'..'9').toList() }
        .map { it.random() }
        .joinToString("")
}

fun shouldDisplayStrikeThroughPrice(price: String?, strikeThroughPrice: String?): Boolean {
    if (price.isNullOrBlank() || strikeThroughPrice.isNullOrBlank()) {
        return false
    }
    return try {
        val cleanPrice = price.replace(Regex("[^0-9.]"), "").toDouble()
        val cleanStrikePrice = strikeThroughPrice.replace(Regex("[^0-9.]"), "").toDouble()
        cleanStrikePrice > 0 && cleanStrikePrice > cleanPrice
    } catch (e: NumberFormatException) {
        false
    }
}

@JvmOverloads
fun formatPriceWithStrikethrough(
    price: String?,
    strikeThroughPrice: String?,
    currencyPrefix: String = ""
): CharSequence {
    if (price.isNullOrBlank()) {
        return ""
    }

    val displayPrice = "$currencyPrefix${price.trim()}"
    if (!shouldDisplayStrikeThroughPrice(price, strikeThroughPrice)) {
        return displayPrice
    }

    val displayStrikePrice = "$currencyPrefix${strikeThroughPrice!!.trim()}"
    val strikeSpannable = SpannableString(displayStrikePrice).apply {
        setSpan(
            StrikethroughSpan(),
            0,
            length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    return SpannableStringBuilder()
        .append(displayPrice)
        .append("  ")
        .append(strikeSpannable)
}
