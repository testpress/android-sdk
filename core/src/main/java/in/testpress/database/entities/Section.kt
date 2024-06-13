package `in`.testpress.database.entities

data class Section(
    val id: Long?,
    val order: Long?,
    val name: String?,
    val duration: String?,
    val cutOff: Long?,
    val instructions: String?,
    val parent: Any?
)
