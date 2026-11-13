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
        val hasValidStructure = NUMERICAL_PATTERN.matcher(value).matches()
        val noConsecutiveDotsOrSlashes = !value.contains("..") && !value.contains("//")
        val noDotAfterSlash = !value.contains("/.")
        val noSlashAfterDot = !value.contains("./")

        return hasValidStructure && noConsecutiveDotsOrSlashes && noDotAfterSlash && noSlashAfterDot
    }
}

