package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.AttemptAnswer;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.models.AttemptQuestion;
import in.testpress.models.greendao.Language;
import in.testpress.models.greendao.LanguageDao;
import in.testpress.models.greendao.TestpressSDK;
import in.testpress.util.WebViewUtils;

public class TestQuestionFragment extends Fragment {

    static final String PARAM_ATTEMPT_ITEM = "attemptItem";
    static final String PARAM_QUESTION_INDEX = "questionIndex";
    static final String PARAM_SELECTED_LANGUAGE = "selectedLanguage";
    private AttemptItem attemptItem;
    private Integer index;
    private List<Integer> selectedOptions;
    private View view;
    private WebView questionsView;
    private WebViewUtils webViewUtils;
    private Language selectedLanguage;

    static TestQuestionFragment getInstance(AttemptItem attemptItem, int questionIndex,
                                            Language selectedLanguage) {

        TestQuestionFragment testQuestionFragment = new TestQuestionFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(TestQuestionFragment.PARAM_ATTEMPT_ITEM, attemptItem);
        bundle.putInt(TestQuestionFragment.PARAM_QUESTION_INDEX, questionIndex);
        //TODO selectedLanguage is null
        bundle.putString(TestQuestionFragment.PARAM_SELECTED_LANGUAGE, selectedLanguage.getCode());
        testQuestionFragment.setArguments(bundle);
        return testQuestionFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attemptItem = getArguments().getParcelable(PARAM_ATTEMPT_ITEM);
        index = getArguments().getInt(PARAM_QUESTION_INDEX);
        selectedLanguage = TestpressSDK.getLanguageDao(getContext()).queryBuilder().where(LanguageDao.Properties.Code.eq(getArguments().getString(PARAM_SELECTED_LANGUAGE))).list().get(0);
        selectedOptions = new ArrayList<>(attemptItem.getSelectedAnswers());
    }

    @SuppressLint({"SetTextI18n", "AddJavascriptInterface"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.testpress_fragment_test_question, container, false);
            questionsView = (WebView) view.findViewById(R.id.question);
            final String optionType = attemptItem.getAttemptQuestion().getType();
            questionsView.addJavascriptInterface(new OptionsSelectionListener(),
                    "OptionsSelectionListener");

            attemptItem.saveAnswers(attemptItem.getSelectedAnswers());
            attemptItem.setCurrentReview(attemptItem.getReview());
            webViewUtils = new WebViewUtils(questionsView) {
                @Override
                public String getHeader() {
                    return super.getHeader() + getTestEngineHeader();
                }

                @Override
                public String getJavascript(Context context) {
                    String javascript = super.getJavascript(context);
                    List<Integer> selectedAnswers = attemptItem.getSavedAnswers();
                    if (!selectedAnswers.isEmpty()) {
                        if (optionType.equals("R")) {
                            javascript += getRadioButtonInitializer(selectedAnswers.get(0));
                        } else {
                            javascript += getCheckBoxInitializer(selectedAnswers);
                        }
                    }
                    return javascript;
                }
            };
            webViewUtils.initWebView(getQuestionItemHtml(), getActivity());
        }
        return view;
    }

    private String getQuestionItemHtml() {
        AttemptQuestion attemptQuestion = attemptItem.getAttemptQuestion();
        ArrayList<AttemptQuestion> translations = attemptQuestion.getTranslations();
        if (translations.size() > 0 && selectedLanguage != null &&
                !selectedLanguage.getCode().equals(attemptQuestion.getLanguage())) {

            for (AttemptQuestion translation : translations) {
                if (translation.getLanguage().equals(selectedLanguage.getCode())) {
                    attemptQuestion = translation;
                }
            }
        }
        String htmlContent = "<div>";

        // Add index
        htmlContent += "<div style='padding-right:10px; padding-left:10px;'>" +
                            "<div class='question-index'>" + index + "</div>";

        // Add direction if present
        if (attemptQuestion.getDirection() != null && !attemptQuestion.getDirection().isEmpty()) {
            htmlContent += "" +
                    "<div class='question' style='padding-bottom: 0px;'>" +
                        attemptQuestion.getDirection() +
                    "</div>";
        }
        // Add question
        htmlContent += "" +
                "<div class='question' style='padding-bottom: 10px;'>" +
                    attemptQuestion.getQuestionHtml() +
                "</div></div>";
        // Add options
        htmlContent += "<hr><table width='100%' style='margin-top:0px; margin-bottom:20px;'>";
        for (int i = 0; i < attemptQuestion.getAttemptAnswers().size(); i++) {
            AttemptAnswer attemptAnswer = attemptQuestion.getAttemptAnswers().get(i);
            if (attemptItem.getAttemptQuestion().getType().equals("R")) {
                htmlContent += "\n" + WebViewUtils.getRadioButtonOptionWithTags(
                        attemptAnswer.getTextHtml(), attemptAnswer.getId());
            } else {
                htmlContent += "\n" + WebViewUtils.getCheckBoxOptionWithTags(
                        attemptAnswer.getTextHtml(), attemptAnswer.getId());
            }
        }
        htmlContent += "</table>";

        // Add review later button
        htmlContent += "<div style='padding-left:60px; padding-right:60px; padding-bottom:20px;'>" +
                "<button class='" + (attemptItem.getCurrentReview() ? "mark-button'" : "unmark-button'") +
                    "onClick='reviewButtonClick(this)' >" +
                        (attemptItem.getCurrentReview() ? "MARKED" : "MARK FOR LATER") +
                "</button>";

        return htmlContent + "</div>";
    }

    private class OptionsSelectionListener {

        @JavascriptInterface
        public void onCheckedChange(String id, boolean checked, boolean radioOption) {
            if (checked) {
                if (radioOption) {
                    selectedOptions = new ArrayList<Integer>();
                }
                selectedOptions.add(Integer.parseInt(id));
            } else {
                selectedOptions.remove((Integer) Integer.parseInt(id));
            }
            attemptItem.saveAnswers(selectedOptions);
        }

        @JavascriptInterface
        public void onMarkStateChange() {
            attemptItem.setCurrentReview(!attemptItem.getCurrentReview());
        }
    }

    @Override
    public void onDestroyView() {
        final ViewGroup viewGroup = (ViewGroup) questionsView.getParent();
        if (viewGroup != null) {
            // Remove webView from its parent before destroy to support below kitkat
            viewGroup.removeView(questionsView);
        }
        questionsView.destroy();
        super.onDestroyView();
    }

    public void update() {
        webViewUtils.loadHtml(getQuestionItemHtml());
    }

}
