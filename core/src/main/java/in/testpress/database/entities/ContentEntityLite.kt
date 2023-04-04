package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContentEntityLite(@PrimaryKey val id: Long, var type: Int) : BaseContentEntity()