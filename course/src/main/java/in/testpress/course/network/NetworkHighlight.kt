package `in`.testpress.course.network

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class PositionDeserializer : JsonDeserializer<List<Double>?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<Double>? {
        if (json == null || json.isJsonNull) return null
        
        return when {
            json.isJsonArray -> {
                val array = json.asJsonArray
                if (array.size() > 0 && array[0].isJsonArray) {
                    // Nested array: [[x1, y1, x2, y2]] -> flatten to [x1, y1, x2, y2]
                    array[0].asJsonArray.mapNotNull { it.asDouble }
                } else {
                    // Flat array: [x1, y1, x2, y2]
                    array.mapNotNull { it.asDouble }
                }
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                json.asString.split(",").mapNotNull { it.toDoubleOrNull() }
            }
            else -> null
        }
    }
}

data class NetworkHighlight(
    val id: Long? = null,
    @SerializedName("page_number")
    val pageNumber: Int? = null,
    @SerializedName("selected_text")
    val selectedText: String? = null,
    val notes: String? = null,
    val color: String? = null,
    @JsonAdapter(PositionDeserializer::class)
    val position: List<Double>? = null,
    val created: String? = null,
    val modified: String? = null
)

