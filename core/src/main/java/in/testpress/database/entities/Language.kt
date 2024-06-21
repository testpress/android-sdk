package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Language(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val code: String? = null,
    val title: String? = null,
    val examId: Long? = null
)