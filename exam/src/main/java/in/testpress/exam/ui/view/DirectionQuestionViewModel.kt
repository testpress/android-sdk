package `in`.testpress.exam.ui.view

import `in`.testpress.util.WebViewUtils
import androidx.lifecycle.ViewModel

class DirectionQuestionViewModel: ViewModel() {
    private var previous: String = " "
    private lateinit var current: String

    fun prepare(current: String): String {
        var html = ""
        this.current = current
        html += if (previous == current) {
            getSimilarQuestion()
        } else {
            getNewQuestion()
        }
        html += createDirectionToggleButton()
        return html
    }

    private fun getSimilarQuestion(): String {
        return if (WebViewUtils.isDirectionButtonStateVisible) {
            createVisibleQuestion()
        } else {
            createHidedQuestion()
        }
    }

    private fun createVisibleQuestion(): String {
        return "<div class='question' id='direction' style='padding-bottom: 0px;'>" +
                current +
                "</div>"
    }

    private fun createHidedQuestion(): String {
        return "<div class='question' id='direction' style='padding-bottom: 0px; display: none;'>" +
                current +
                "</div>"
    }

    private fun getNewQuestion(): String {
        WebViewUtils.isDirectionButtonStateVisible = true
        previous = current
        return createVisibleQuestion()
    }

    private fun createDirectionToggleButton(): String {
        return "\n<button id='direction-toggle-button' onclick = 'toggleDirectionVisibility()'>Hide/Show Direction</button>"
    }
}