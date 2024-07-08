package `in`.testpress.course.network

import `in`.testpress.database.entities.*

class NetworkOfflineQuestionResponse(
    val directions: List<Direction>,
    val subjects: List<Subject>,
    val sections: List<Section>,
    val examQuestions: List<ExamQuestion>,
    val questions: List<Question>
){
    fun extractUrls(): List<String> {
        val regexs = listOf(Regex("""src=["'](.*?)["']"""), Regex("""(?:url|src)\((.*?)\)"""))
        val urls = mutableListOf<String>()

        regexs.forEach { urlPattern ->
            this.directions.forEach { direction ->
                direction.html?.let { html ->
                    urlPattern.findAll(html).forEach { matchResult ->
                        val url = matchResult.groupValues[1].trim()
                        urls.add(url)
                    }
                }
            }

            this.sections.forEach { section ->
                section.instructions?.let { instructions ->
                    urlPattern.findAll(instructions).forEach { matchResult ->
                        val url = matchResult.groupValues[1].trim()
                        urls.add(url)
                    }
                }
            }

            this.questions.forEach { question ->
                question.questionHtml?.let { questionHtml ->
                    urlPattern.findAll(questionHtml).forEach { matchResult ->
                        val url = matchResult.groupValues[1].trim()
                        urls.add(url)
                    }
                }
                question.translations.forEach { translation ->
                    translation.questionHtml?.let { translationHtml ->
                        urlPattern.findAll(translationHtml).forEach { matchResult ->
                            val url = matchResult.groupValues[1].trim()
                            urls.add(url)
                        }
                    }
                }
                question.answers.forEach { answer ->
                    answer.textHtml?.let { textHtml ->
                        urlPattern.findAll(textHtml).forEach { matchResult ->
                            val url = matchResult.groupValues[1].trim()
                            urls.add(url)
                        }
                    }
                }
            }
        }

        return urls
    }
}