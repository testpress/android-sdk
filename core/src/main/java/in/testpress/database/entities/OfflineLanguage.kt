package `in`.testpress.database.entities

import androidx.room.Entity

@Entity
data class Language(
    val id: Long? = null,
    val code: String? = null,
    val title: String? = null,
    val examId: Long? = null
)