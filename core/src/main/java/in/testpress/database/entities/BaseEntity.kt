package `in`.testpress.database.entities

open class BaseContentStateEntity {
    var order: Int? = null
    var chapterId: Long? = null
    var freePreview: Boolean? = null
    var title: String? = null
    var courseId: Long? = null
    var examId: Long? = null
    var contentId: Long? = null
    var videoId: Long? = null
    var attachmentId: Long? = null
    var contentType: String? = null
    var icon: String? = null
    var start: String? = null
    var end: String? = null
    var treePath: String? = null

    fun getFormattedStartDate(): String {   // Input "2023-01-23T18:38:57.515885+05:30"
        return if (start == null || start?.equals("")!!){
            ""
        } else if (start?.length == 25){
            start!!
        } else {
            val one = start?.split(".")
            val two = one?.get(1)?.split("+")
            "${one?.get(0)}+${two?.get(1)}"
        }
    }                                       // Output "2023-01-23T18:38:57+05:30"

    fun getFormattedEndDate(): String {     // Input "2023-01-23T18:38:57.515885+05:30"
        return if (end == null || end?.equals("")!!){
            ""
        } else if (end?.length == 25){
            end!!
        } else {
            val one = end?.split(".")
            val two = one?.get(1)?.split("+")
            "${one?.get(0)}+${two?.get(1)}"
        }
    }                                       // Output "2023-01-23T18:38:57+05:30"
}
