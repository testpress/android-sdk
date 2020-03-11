package `in`.testpress.course.network

import `in`.testpress.database.StreamEntity
import `in`.testpress.models.greendao.Stream

data class NetworkStream(
    val id: Long,
    val url: String? = null,
    val format: String = "",
    val videoId: Long
)

fun NetworkStream.asGreenDaoModel(): Stream {
    return Stream(this.id, this.format, this.url, this.videoId)
}

fun createStream(stream: NetworkStream): StreamEntity {
    return StreamEntity(
        id = stream.id,
        url = stream.url,
        format = stream.format,
        videoId = stream.videoId
    )
}

fun NetworkStream.asDatabaseModel(): StreamEntity {
    return createStream(this)
}

fun List<NetworkStream>.asDatabaseModels(): List<StreamEntity> {
    return this.map {
        it.asDatabaseModel()
    }
}