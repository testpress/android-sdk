package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Direction(
    @PrimaryKey
    val id: Long? = null,
    val html: String? = null
)
