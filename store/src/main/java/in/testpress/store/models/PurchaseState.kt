package `in`.testpress.store.models

enum class PurchaseState(val value: String) {
    AVAILABLE("available"),
    INSTALLMENT_DUE("installment_due"),
    ENROLLED("enrolled");

    companion object {
        @JvmStatic
        fun fromValue(value: String?): PurchaseState {
            return values().firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: AVAILABLE
        }
    }
}
