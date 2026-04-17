package `in`.testpress.store.data.model

data class NetworkProductOffersResponse(
    val hasDiscount: Boolean = false,
    val finalPrice: String? = null,
    val offers: List<Any> = emptyList(),
    val appliedDiscountName: String? = null,
)

