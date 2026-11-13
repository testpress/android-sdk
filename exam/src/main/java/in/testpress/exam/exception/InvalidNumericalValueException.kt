package `in`.testpress.exam.exception

class InvalidNumericalValueException(
    message: String? = null
) : RuntimeException(message) {
    companion object {
        const val VALIDATION_ERROR_CODE = 422
    }
}

