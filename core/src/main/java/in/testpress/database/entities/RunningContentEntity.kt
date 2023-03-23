package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RunningContentEntity(@PrimaryKey val id: Long) : BaseContentEntity()