package `in`.testpress.exam.util

import java.util.regex.Pattern

object NumericalInputValidator {
    private val NUMERICAL_PATTERN = Pattern.compile("^-?(\\d+(\\.\\d+)?|\\.\\d+)(/-?\\d+(\\.\\d+)?)?$")

    @JvmStatic
    fun isValidNumericalInput(input: String?): Boolean {
        if (input.isNullOrBlank()) {
            return false
        }
        val value = input.trim()
        if (!NUMERICAL_PATTERN.matcher(value).matches()) {
            return false
        }
        if (value.contains('/')) {
            val denominator = value.substringAfter('/').toDoubleOrNull()
            if (denominator == null || denominator == 0.0) {
                return false
            }
        }
        return true
    }
}

