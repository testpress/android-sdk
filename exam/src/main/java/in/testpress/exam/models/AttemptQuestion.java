package in.testpress.exam.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class AttemptQuestion implements Parcelable {

    private String questionHtml;
    private List<AttemptAnswer> answers = new ArrayList<AttemptAnswer>();
    private String subject;
    private String direction;
    private String type;
    private String language;
    private ArrayList<AttemptQuestion> translations = new ArrayList<>();

    // Parcelling part
    public AttemptQuestion(Parcel parcel){
        answers = new ArrayList<AttemptAnswer>();
        parcel.readTypedList(answers, AttemptAnswer.CREATOR);
        questionHtml = parcel.readString();
        subject = parcel.readString();
        type = parcel.readString();
        language = parcel.readString();
        parcel.readTypedList(translations, AttemptQuestion.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(answers);
        parcel.writeString(questionHtml);
        parcel.writeString(subject);
        parcel.writeString(type);
        parcel.writeString(language);
        parcel.writeTypedList(translations);
    }

    public static final Creator<AttemptQuestion> CREATOR = new Creator<AttemptQuestion>() {
        public AttemptQuestion createFromParcel(Parcel parcel) {
            return new AttemptQuestion(parcel);
        }

        public AttemptQuestion[] newArray(int size) {
            return new AttemptQuestion[size];
        }
    };

    /**
     *
     * @return
     * The questionHtml
     */
    public String getQuestionHtml() {
        return questionHtml;
    }

    /**
     *
     * @param questionHtml
     * The question_html
     */
    public void setQuestionHtml(String questionHtml) {
        this.questionHtml = questionHtml;
    }

    /**
     *
     * @return
     * The answers
     */
    public List<AttemptAnswer> getAttemptAnswers() {
        return answers;
    }

    /**
     *
     * @param attemptAnswers
     * The answers
     */
    public void setAttemptAnswers(List<AttemptAnswer> attemptAnswers) {
        this.answers = attemptAnswers;
    }

    /**
     *
     * @return
     * The subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     *
     * @param subject
     * The subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     *
     * @return
     * The type
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     * The type
     */
    public void setType(String type) {
        this.type = type;
    }


    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public ArrayList<AttemptQuestion> getTranslations() {
        return translations;
    }

    public void setTranslations(ArrayList<AttemptQuestion> translations) {
        this.translations = translations;
    }
}