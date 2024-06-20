package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Section(
    @PrimaryKey
    val id: Long?,
    val order: Long?,
    val name: String?,
    val duration: String?,
    val cutOff: Long?,
    val instructions: String?,
    val parent: Long?
)
