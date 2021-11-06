package in.testpress.exam.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.testpress.models.greendao.AttemptSection;
import in.testpress.util.CommonUtils;

public class AttemptItem implements Parcelable {

    private Integer id;
    private String url;
    private AttemptQuestion question;
    private List<Integer> selectedAnswers = new ArrayList<Integer>();
    private Boolean review;
    private List<Integer> savedAnswers = new ArrayList<Integer>();
    private Integer index;
    private Boolean currentReview;
    private String shortText;
    private String currentShortText;
    private AttemptSection attemptSection;
    private String essayText;
    private String localEssayText;
    private List<String> files = new ArrayList<String>();
    private List<String> unSyncedFiles = new ArrayList<String>();

    AttemptItem() {
        selectedAnswers = new ArrayList<Integer>();
        savedAnswers = new ArrayList<Integer>();
    }

    // Parcelling part
    protected AttemptItem(Parcel in) {
        id = in.readInt();
        url = in.readString();
        question = in.readParcelable(AttemptQuestion.class.getClassLoader());
        byte tmpReview = in.readByte();
        review = tmpReview == 0 ? null : tmpReview == 1;
        if (in.readByte() == 0) {
            index = null;
        } else {
            index = in.readInt();
        }
        byte tmpCurrentReview = in.readByte();
        currentReview = tmpCurrentReview == 0 ? null : tmpCurrentReview == 1;
        shortText = in.readString();
        currentShortText = in.readString();
        attemptSection = in.readParcelable(AttemptSection.class.getClassLoader());
        in.readList(selectedAnswers, Integer.class.getClassLoader());
        in.readList(savedAnswers, Integer.class.getClassLoader());
        in.readList(files, String.class.getClassLoader());
        in.readList(unSyncedFiles, String.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(url);
        dest.writeParcelable(question, flags);
        dest.writeByte((byte) (review == null ? 0 : review ? 1 : 2));
        if (index == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(index);
        }
        dest.writeByte((byte) (currentReview == null ? 0 : currentReview ? 1 : 2));
        dest.writeString(shortText);
        dest.writeString(currentShortText);
        dest.writeParcelable(attemptSection, flags);
        dest.writeList(selectedAnswers);
        dest.writeList(savedAnswers);
        dest.writeList(files);
        dest.writeList(unSyncedFiles);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AttemptItem> CREATOR = new Creator<AttemptItem>() {
        @Override
        public AttemptItem createFromParcel(Parcel in) {
            return new AttemptItem(in);
        }

        @Override
        public AttemptItem[] newArray(int size) {
            return new AttemptItem[size];
        }
    };

    public void saveAnswers(List<Integer> savedAnswers) {
        this.savedAnswers = savedAnswers;
    }

    public List<Integer> getSavedAnswers() {
        return savedAnswers;
    }

    private boolean isSelectedAnswersSynced() {
        Collections.sort(savedAnswers);
        Collections.sort(selectedAnswers);
        return savedAnswers.equals(selectedAnswers);
    }

    private boolean isMarkForReviewSynced() {
        return getCurrentReview().equals(getReview());
    }

    private boolean isShortTextSynced() {
        if (shortText != null) {
            return shortText.equals(currentShortText);
        }

        return currentShortText == shortText;
    }

    private boolean isEssaySynced() {
        if (essayText != null) {
            return essayText.equals(localEssayText);
        }

        return essayText == localEssayText;
    }

    public Boolean hasChanged() {
        return !isSelectedAnswersSynced() || !isMarkForReviewSynced() || !isShortTextSynced() || !isEssaySynced();
    }

    /**
     *
     * @return
     * The url
     */
    public String getUrl() {
        return url;
    }

    public String getUrlFrag() {
        try {
            URL urlFrag = new URL(url);
            return urlFrag.getFile().substring(1);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     *
     * @param url
     * The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return
     * The index
     */
    public Integer getIndex() {
        return index;
    }

    /**
     *
     * @param index
     * The index
     */
    public void setIndex(Integer index) {
        this.index = index;
    }

    /**
     *
     * @return
     * The question
     */
    public AttemptQuestion getAttemptQuestion() {
        return question;
    }

    /**
     *
     * @param attemptQuestion
     * The question
     */
    public void setAttemptQuestion(AttemptQuestion attemptQuestion) {
        this.question = attemptQuestion;
    }

    /**
     *
     * @return
     * The selectedAnswers
     */
    public List<Integer> getSelectedAnswers() {
        return selectedAnswers;
    }

    /**
     *
     * @param selectedAnswers
     * The selected_answers
     */
    public void setSelectedAnswers(List<Integer> selectedAnswers) {
        this.selectedAnswers = selectedAnswers;
    }

    /**
     *
     * @return
     * The review
     */
    public Boolean getReview() {
        return Boolean.TRUE.equals(review);
    }

    /**
     *
     * @param review
     * The review
     */
    public void setReview(Boolean review) {
        this.review = review;
    }

    public void setCurrentReview(Boolean currentReview) {
        this.currentReview = currentReview;
    }

    public Boolean getCurrentReview() {
        return Boolean.TRUE.equals(currentReview);
    }

    public String getShortText() {
        return shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }

    public String getCurrentShortText() {
        return currentShortText;
    }

    public void setCurrentShortText(String currentShortText) {
        this.currentShortText = currentShortText;
    }

    public AttemptSection getAttemptSection() {
        return attemptSection;
    }

    public void setAttemptSection(AttemptSection attemptSection) {
        this.attemptSection = attemptSection;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEssayText() {
        return essayText;
    }

    public void setEssayText(String essayText) {
        this.essayText = essayText;
    }

    public void setLocalEssayText(String localEssayText) {
        this.localEssayText = localEssayText;
    }

    public String getLocalEssayText() {
        return localEssayText;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public List<String> getUnSyncedFiles() {
        return unSyncedFiles;
    }

    public void setUnSyncedFiles(List<String> files) {
        this.unSyncedFiles = files;
    }

    public String getFiletypeDisplayHtml() {
        String htmlContent = "";
        for (int i=0; i < getUnSyncedFiles().size(); i++) {
            htmlContent += String.format("<li> File %d</li>", i + 1);
        }
        htmlContent += "<div class='review_later_button_layout'>" +
                "<button class='upload-button' onClick='onFileUploadClick(this)'> Upload File </button>" +
                "<button class='clear-upload-button' onClick='onClearUploadsClick(this)'> Clear Uploads </button>" +
                "</div>";
        return htmlContent;
    }
}
