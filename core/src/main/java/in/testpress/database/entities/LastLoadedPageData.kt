package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// This entity is used for storing next & prev page to be fetched for any resource in RemoteMediator
@Entity
data class LastLoadedPageData(
        @PrimaryKey
        val resourceType: String,
        val previous: Int?,
        val next: Int?
)
