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

    fun replaceNetworkUrlWithLocalUrl(urlToLocalPaths: HashMap<String, String>) {

        urlToLocalPaths.map { urlToLocalPath ->

            this.directions.forEach { direction ->
                direction.html?.let { directionHtml ->
                    direction.html = directionHtml.replace(urlToLocalPath.key, urlToLocalPath.value)
                }
            }

            this.sections.forEach { section ->
                section.instructions?.let { instructions ->
                    section.instructions =
                        instructions.replace(urlToLocalPath.key, urlToLocalPath.value)
                }
            }

            this.questions.forEach { question ->
                question.questionHtml?.let { questionHtml ->
                    question.questionHtml =
                        questionHtml.replace(urlToLocalPath.key, urlToLocalPath.value)
                }

                question.translations.forEach { translation ->
                    translation.questionHtml?.let { translationHtml ->
                        translation.questionHtml =
                            translationHtml.replace(urlToLocalPath.key, urlToLocalPath.value)
                    }
                }

                question.answers.forEach { answer ->
                    answer.textHtml?.let { textHtml ->
                        answer.textHtml = textHtml.replace(urlToLocalPath.key, urlToLocalPath.value)
                    }
                }
            }
        }
    }
}