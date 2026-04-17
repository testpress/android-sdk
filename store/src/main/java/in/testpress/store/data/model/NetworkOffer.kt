package `in`.testpress.store.data.model

data class NetworkOffer(
    val id: Int,
    val name: String? = null,
    val description: String? = null,
    val offerPrice: String? = null,
    val benefitType: String? = null,
    val benefitValue: String? = null,
    val endDatetime: String? = null,
)

