package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntity(
    @PrimaryKey
    var id: Long? = null,
    val url: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val displayName: String? = null,
    val photo: String? = null,
    val largeImage: String? = null,
    val mediumImage: String? = null,
    val mediumSmallImage: String? = null,
    val smallImage: String? = null,
    val xSmallImage: String? = null,
    val miniImage: String? = null
)
