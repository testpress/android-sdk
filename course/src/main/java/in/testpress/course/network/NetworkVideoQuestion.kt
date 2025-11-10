package `in`.testpress.course.network

import com.google.gson.annotations.SerializedName


data class NetworkVideoQuestionResponse(
    @SerializedName("results")
    val results: List<NetworkVideoQuestion>
)


data class NetworkVideoQuestion(
    @SerializedName("id")
    val id: Long,

    @SerializedName("position")
    val position: Int,

    @SerializedName("order")
    val order: Int,

    @SerializedName("question")
    val question: NetworkQuestion
)


data class NetworkQuestion(
    @SerializedName("id")
    val id: Long,

    @SerializedName("type")
    val type: String, // "R", "C", or "G"

    @SerializedName("question_html")
    val questionHtml: String,

    @SerializedName("answers")
    val answers: List<NetworkAnswer>? // Nullable for Gap-Fill (G)
)


data class NetworkAnswer(
    @SerializedName("id")
    val id: Long,

    @SerializedName("is_correct")
    val isCorrect: Boolean,

    @SerializedName("text_html")
    val textHtml: String
)

