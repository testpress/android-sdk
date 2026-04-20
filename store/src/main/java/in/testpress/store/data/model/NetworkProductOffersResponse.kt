package `in`.testpress.store.data.model

data class NetworkProductOffersResponse(
    val hasDiscount: Boolean = false,
    val finalPrice: String? = null,
    val offers: List<NetworkOffer> = emptyList(),
    val appliedDiscountName: String? = null,
)

