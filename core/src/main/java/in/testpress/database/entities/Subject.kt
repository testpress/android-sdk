package `in`.testpress.database.entities

import androidx.room.Entity

@Entity
data class Subject(
    val id: Long? = null,
    val name: String? = null
)