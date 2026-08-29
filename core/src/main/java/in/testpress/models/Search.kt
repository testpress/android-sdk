package `in`.testpress.models

data class SearchApiResponse(
    val results: List<SearchResult>,
    val nextPage: Int?
)

data class SearchResult(
    val title: String?,
    val highlight: Highlight?,
    val active: Boolean?,
    val type: String?,
    val id: Int?
)

data class Highlight(
    val title: String?
)