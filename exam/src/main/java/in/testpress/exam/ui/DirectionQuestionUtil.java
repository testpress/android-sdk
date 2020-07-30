package in.testpress.exam.ui;

import in.testpress.util.WebViewUtils;

public class DirectionQuestionUtil {
    public static String previousDirectionQuestion = " ";
    public static String addDirectionQuestionAndButton(String currentDirectionHtml) {
        String html = "";
        if (previousDirectionQuestion.equals(currentDirectionHtml)) {
            html += handleSimilarQuestion(currentDirectionHtml);
        } else {
            html += handleNewDirectionQuestion(currentDirectionHtml);
        }
        html += getDirectionButton();
        return html;
    }

    public static String handleSimilarQuestion(String currentDirectionHtml) {
        if (WebViewUtils.directionButtonStateVisible) {
            return getVisibleDirectionQuestion(currentDirectionHtml);
        } else {
            return getHiddenDirectionQuestion(currentDirectionHtml);
        }
    }

    public static String getVisibleDirectionQuestion(String currentDirectionHtml) {
        return  "<div class='question' id='direction' style='padding-bottom: 0px;'>" +
                currentDirectionHtml +
                "</div>";
    }

    public static String getHiddenDirectionQuestion(String currentDirectionHtml) {
        return "<div class='question' id='direction' style='padding-bottom: 0px; display: none;'>" +
                currentDirectionHtml +
                "</div>";
    }

    public static String handleNewDirectionQuestion(String currentDirectionHtml) {
        WebViewUtils.directionButtonStateVisible = true;
        previousDirectionQuestion = currentDirectionHtml;
        return  getVisibleDirectionQuestion(currentDirectionHtml);
    }

    public static String getDirectionButton() {
        return "\n" + WebViewUtils.getButtonToShowOrHideDirection();
    }
}
