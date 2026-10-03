package `in`.testpress.store.models

import `in`.testpress.core.TestpressException

enum class PurchaseState(val value: String) {
    AVAILABLE("available"),
    INSTALLMENT_DUE("installment_due"),
    ENROLLED("enrolled");

    companion object {
        @JvmStatic
        fun fromValue(value: String?): PurchaseState {
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: AVAILABLE
        }

        @JvmStatic
        fun isAlreadyPurchased(exception: TestpressException?): Boolean {
            if (exception == null) return false
            val errorBody = exception.errorBodyString.orEmpty()
            val errorMessage = exception.message.orEmpty()
            return errorBody.contains("already purchased", ignoreCase = true) ||
                    errorMessage.contains("already purchased", ignoreCase = true)
        }
    }
}
