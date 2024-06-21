package `in`.testpress.course.network

import `in`.testpress.database.entities.*

class NetworkOfflineQuestionResponse(
    val directions: List<Direction>,
    val subjects: List<Subject>,
    val sections: List<Section>,
    val examQuestions: List<ExamQuestion>,
    val questions: List<Question>
)