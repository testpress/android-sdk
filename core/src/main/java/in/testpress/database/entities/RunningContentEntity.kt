package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Entity
data class RunningContentEntity(@PrimaryKey val id: Long): BaseContentStateEntity()