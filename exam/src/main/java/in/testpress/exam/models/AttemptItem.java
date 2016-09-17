package in.testpress.exam.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AttemptItem implements Parcelable {

    private String url;
    private AttemptQuestion question;
    private List<Integer> selectedAnswers = new ArrayList<Integer>();
    private Boolean review;
    private List<Integer> savedAnswers = new ArrayList<Integer>();
    private Integer index;
    private Boolean currentReview;

    AttemptItem() {
        selectedAnswers = new ArrayList<Integer>();
        savedAnswers = new ArrayList<Integer>();
    }

    // Parcelling part
    public AttemptItem(Parcel parcel){
        question = (AttemptQuestion) parcel.readParcelable(AttemptQuestion.class.getClassLoader());
        url = parcel.readString();
        selectedAnswers = new ArrayList<Integer>();
        parcel.readList(selectedAnswers, List.class.getClassLoader());
        savedAnswers = new ArrayList<Integer>();
        parcel.readList(savedAnswers, List.class.getClassLoader());
        review = parcel.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(question, i);
        parcel.writeString(url);
        parcel.writeList(selectedAnswers);
        parcel.writeList(savedAnswers);
        if (review == null) {
            parcel.writeByte((byte) (0));
        } else {
            parcel.writeByte((byte) (review ? 1 : 0)); //if review == true, byte == 1
        }
    }

    public static final Creator CREATOR = new Creator() {
        public AttemptItem createFromParcel(Parcel parcel) {
            return new AttemptItem(parcel);
        }

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

    public Boolean hasChanged() {
        return !savedAnswers.equals(selectedAnswers) || !getCurrentReview().equals(getReview());
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

}
