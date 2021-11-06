package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.api.TestpressExamApiClient;
import in.testpress.exam.models.AttemptAnswer;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.models.AttemptQuestion;
import in.testpress.exam.ui.view.WebView;
import in.testpress.models.FileDetails;
import in.testpress.models.InstituteSettings;
import in.testpress.models.greendao.Language;
import in.testpress.util.ProgressDialog;
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
    private InstituteSettings instituteSettings;
    private TestpressExamApiClient apiClient;

    static TestQuestionFragment getInstance(AttemptItem attemptItem, int questionIndex,
                                            Language selectedLanguage) {

        TestQuestionFragment testQuestionFragment = new TestQuestionFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(TestQuestionFragment.PARAM_ATTEMPT_ITEM, attemptItem);
        bundle.putInt(TestQuestionFragment.PARAM_QUESTION_INDEX, questionIndex);
        bundle.putParcelable(TestQuestionFragment.PARAM_SELECTED_LANGUAGE, selectedLanguage);
        testQuestionFragment.setArguments(bundle);
        return testQuestionFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attemptItem = getArguments().getParcelable(PARAM_ATTEMPT_ITEM);
        index = getArguments().getInt(PARAM_QUESTION_INDEX);
        selectedLanguage = getArguments().getParcelable(PARAM_SELECTED_LANGUAGE);
        selectedOptions = new ArrayList<>(attemptItem.getSelectedAnswers());
        instituteSettings = TestpressSdk.getTestpressSession(getContext()).getInstituteSettings();
        apiClient = new TestpressExamApiClient(getContext());
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

            setInitialDataToAttemptItem();
            webViewUtils = new WebViewUtils(questionsView) {
                @Override
                public String getHeader() {
                    return getQuestionsHeader() + getTestEngineHeader();
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

                @Override
                protected void onLoadFinished() {
                    super.onLoadFinished();
                    if (instituteSettings.getAppToolbarLogo() != null) {
                        webViewUtils.addWatermark(instituteSettings.getAppToolbarLogo());
                    }
                }
            };
            webViewUtils.initWebView(getQuestionItemHtml(), getActivity());
        }
        return view;
    }

    private void setInitialDataToAttemptItem() {
        attemptItem.saveAnswers(attemptItem.getSelectedAnswers());
        attemptItem.setCurrentShortText(attemptItem.getShortText());
        attemptItem.setCurrentReview(attemptItem.getReview());
        attemptItem.setLocalEssayText(attemptItem.getEssayText());
        attemptItem.setUnSyncedFiles(attemptItem.getFiles());
    }

    private String getQuestionItemHtml() {
        AttemptQuestion attemptQuestion = attemptItem.getAttemptQuestion();
        ArrayList<AttemptQuestion> translations = attemptQuestion.getTranslations();
        if (translations.size() > 0 && selectedLanguage != null &&
                !selectedLanguage.getCode().equals(attemptQuestion.getLanguage())) {

            for (AttemptQuestion translation : translations) {
                if (translation.getLanguage().equals(selectedLanguage.getCode())) {
                    translation.setType(attemptQuestion.getType());
                    attemptQuestion = translation;
                }
            }
        }
        String htmlContent = "<div class='question_container' style='font-size:calc(12px + 1.5vw);'>";

        // Add index
        htmlContent += "<div style='padding-right:10px; padding-left:10px;'>";

        htmlContent += "<div class='header'>";
        htmlContent += "<div class='question-index'>" + index + "</div>";
        htmlContent += getMarksHtml(attemptQuestion);
        htmlContent += "</div>";
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

        if (attemptQuestion.getType().equals("R") || attemptQuestion.getType().equals("C")) {
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
        } else if(attemptQuestion.getType().equals("F")) {
            htmlContent += attemptItem.getFiletypeDisplayHtml();
        } else if (attemptQuestion.getType().equals("E")) {
            htmlContent += getEssayQuestionHtml();
        } else {
            boolean numberType = attemptQuestion.getType().equals("N");
            questionsView.setNumberType(numberType);
            String value = attemptItem.getCurrentShortText() != null ? attemptItem.getCurrentShortText() : "";
            htmlContent += "<input class='edit_box' type='text' onpaste='return false'" +
                    "value='" + value + "' oninput='onValueChange(this)' placeholder='YOUR ANSWER'>";
        }
        htmlContent += "</div>";
        // Add review later button
        htmlContent += "<div class='review_later_button_layout'>" +
                "<button class='" + (attemptItem.getCurrentReview() ? "mark-button'" : "unmark-button'") +
                    "onClick='reviewButtonClick(this)' >" +
                        (attemptItem.getCurrentReview() ? "MARKED" : "MARK FOR LATER") +
                "</button></div>";

        return htmlContent;
    }

    private String getMarksHtml(AttemptQuestion attemptQuestion){
        String marksHtml = "<div class='marks-wrapper'>";
        if (attemptQuestion.hasPositiveMarks()){
            marksHtml +=  "<div class='positive-marks'>" +
                    "<div class='label'>Marks</div>" +
                    "<div class='value'>" + attemptQuestion.getMarks() + "</div>" +
                    "</div>";
        }

        if (attemptQuestion.hasNegativeMarks()){
            marksHtml +=  "<div class='negative-marks'>" +
                    "<div class='label'>Negative Marks</div>" +
                    "<div class='value'>- " + attemptQuestion.getNegativeMarks() + "</div>" +
                    "</div>";
        }

        marksHtml += "</div>";
        return marksHtml;
    }
    
    @NotNull
    private String getEssayQuestionHtml() {
        String htmlContent = "<textarea class='essay_topic'  oninput='onEssayValueChange(this)' rows='10'>";

        if (attemptItem.getLocalEssayText() != null) {
            htmlContent += attemptItem.getLocalEssayText();
        }
        htmlContent += "</textarea>";
        return htmlContent;
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
        public void onClearUploadsClick() {
            attemptItem.setUnSyncedFiles(new ArrayList<String>());
        }

        @JavascriptInterface
        public void onFileUploadClick() {
            pickFile();
            Toast.makeText(requireContext(), "Hello", Toast.LENGTH_LONG).show();
        }

        @JavascriptInterface
        public void onMarkStateChange() {
            attemptItem.setCurrentReview(!attemptItem.getCurrentReview());
        }

        @JavascriptInterface
        public void onTextValueChange(String value) {
            attemptItem.setCurrentShortText(value.trim());
        }

        @JavascriptInterface
        public void onEssayValueChange(String value) {
            attemptItem.setLocalEssayText(value.trim());
        }
    }

    void pickFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
       if(getParentFragment() != null) {
           getParentFragment().startActivityForResult(intent, 42);
       } else {
           startActivityForResult(intent, 42);
       }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("TAG", "onActivityResult: " + resultCode + requestCode + data.toString());
        if (requestCode == 42 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                onFilePicked(data.getData().getPath());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onFilePicked(String filePath) {
        Log.d("TAG", "onFilePicked: 1" + filePath);
        final AlertDialog progressDialog = new ProgressDialog().showProgressDialog(getContext(), "Uploading");
        progressDialog.show();
        apiClient.upload(filePath)
                .enqueue(new TestpressCallback<FileDetails>() {
                    @Override
                    public void onSuccess(FileDetails fileDetails) {
                        Log.d("TAG", "onSuccess: " + fileDetails.getUrl());
                        ArrayList<String> fileURLs = new ArrayList<>(attemptItem.getUnSyncedFiles());
                        fileURLs.add(fileDetails.getId());
                        attemptItem.setUnSyncedFiles(fileURLs);
                        progressDialog.hide();
                        Toast.makeText(getContext(), "Uploaded files successfully", Toast.LENGTH_LONG).show();
                        update();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        Log.d("TAG", "onException: " + exception.getMessage());
                        progressDialog.hide();
                    }
                });
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
