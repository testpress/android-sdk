package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeys(
        @PrimaryKey
        val resourceType: String,
        val prevKey: Int?,
        val nextKey: Int?
)
