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
    private String marks;
    private String negativeMarks;

    public AttemptQuestion(String questionHtml, List<AttemptAnswer> answers, String subject,
                           String direction, String type, String language,
                           ArrayList<AttemptQuestion> translations, String marks,
                           String negativeMarks) {
        this.questionHtml = questionHtml;
        this.answers = answers;
        this.subject = subject;
        this.direction = direction;
        this.type = type;
        this.language = language;
        this.translations = translations;
        this.marks = marks;
        this.negativeMarks = negativeMarks;
    }

    // Parcelling part
    public AttemptQuestion(Parcel parcel){
        answers = new ArrayList<AttemptAnswer>();
        parcel.readTypedList(answers, AttemptAnswer.CREATOR);
        questionHtml = parcel.readString();
        subject = parcel.readString();
        type = parcel.readString();
        language = parcel.readString();
        parcel.readTypedList(translations, AttemptQuestion.CREATOR);
        marks = parcel.readString();
        negativeMarks = parcel.readString();
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
        parcel.writeString(marks);
        parcel.writeString(negativeMarks);
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

    public String getMarks() {
        return marks;
    }

    public void setMarks(String marks) {
        this.marks = marks;
    }

    public String getNegativeMarks() {
        return negativeMarks;
    }

    public void setNegativeMarks(String negativeMarks) {
        this.negativeMarks = negativeMarks;
    }

    public boolean hasNegativeMarks(){
        return this.negativeMarks != null && !this.negativeMarks.equals("0.00") && !this.negativeMarks.isEmpty();
    }

    public boolean hasPositiveMarks(){
        return this.marks != null && !this.marks.equals("0.00") && !this.marks.isEmpty();
    }
}