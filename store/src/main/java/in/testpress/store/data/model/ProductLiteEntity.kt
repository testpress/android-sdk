package `in`.testpress.store.data.model

data class NetworkProductLite(
    val id: Int?,
    val title: String?,
    val slug: String?,
    val images: List<NetworkImage>? = null,
    val categoryId: Int?,
    val contentsCount: Int = 0,
    val chaptersCount: Int = 0,
    val order: Int?,
    val price: String?
)