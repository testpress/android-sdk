package in.testpress.exam.models;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Attempt implements Parcelable {

    private String url;
    private Integer id;
    private String date;
    private Integer totalQuestions;
    private String score;
    private String rank;
    private String reviewUrl;
    private String questionsUrl;
    private Integer correctCount;
    private Integer incorrectCount;
    private String lastStartedTime;
    private String remainingTime;
    private String timeTaken;
    private String state;
    private String percentile;

    // Parcelling part
    public Attempt(Parcel parcel){
        id = parcel.readInt();
        url = parcel.readString();
        date = parcel.readString();
        totalQuestions = parcel.readInt();
        score = parcel.readString();
        rank = parcel.readString();
        reviewUrl = parcel.readString();
        questionsUrl = parcel.readString();
        correctCount = parcel.readInt();
        incorrectCount = parcel.readInt();
        lastStartedTime = parcel.readString();
        remainingTime = parcel.readString();
        timeTaken = parcel.readString();
        state = parcel.readString();
        percentile = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(url);
        parcel.writeString(date);
        parcel.writeInt(totalQuestions);
        parcel.writeString(score);
        parcel.writeString(rank);
        parcel.writeString(reviewUrl);
        parcel.writeString(questionsUrl);
        parcel.writeInt(correctCount);
        parcel.writeInt(incorrectCount);
        parcel.writeString(lastStartedTime);
        parcel.writeString(remainingTime);
        parcel.writeString(timeTaken);
        parcel.writeString(state);
        parcel.writeString(percentile);
    }

    public static final Creator CREATOR = new Creator() {
        public Attempt createFromParcel(Parcel parcel) {
            return new Attempt(parcel);
        }

        public Attempt[] newArray(int size) {
            return new Attempt[size];
        }
    };

    /**
     *
     * @return
     * The url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     * The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlFrag() {
        try {
            URL fragUrl = new URL(url);
            return fragUrl.getFile().substring(1);
        } catch (Exception e) {
            return null;
        }
    }

    public String getStartUrlFrag() {
        return getUrlFrag() + "start/";
    }

    public String getEndUrlFrag() {
        return getUrlFrag() + "end/";
    }

    public String getHeartBeatUrlFrag() {
        return getUrlFrag() + "heartbeat/";
    }

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The date
     */
    public String getDate() {
        return formatDate(date);
    }

    @SuppressLint("SimpleDateFormat")
    public String formatDate(String inputString) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            date = simpleDateFormat.parse(inputString);
            DateFormat dateformat = DateFormat.getDateInstance();
            return dateformat.format(date);
        } catch (ParseException e) {
        }
        return null;
    }

    /**
     *
     * @param date
     * The date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     *
     * @return
     * The totalQuestions
     */
    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    /**
     *
     * @param totalQuestions
     * The total_questions
     */
    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    /**
     *
     * @return
     * The score
     */
    public String getScore() {
        return score;
    }

    /**
     *
     * @param score
     * The score
     */
    public void setScore(String score) {
        this.score = score;
    }

    /**
     *
     * @return
     * The rank
     */
    public String getRank() {
        return rank;
    }

    /**
     *
     * @param rank
     * The rank
     */
    public void setRank(String rank) {
        this.rank = rank;
    }

    /**
     *
     * @return
     * The reviewUrl
     */
    public String getReviewUrl() {
        return reviewUrl;
    }

    public String getReviewFrag() {
        try {
            URL url = new URL(reviewUrl);
            return url.getFile().substring(1);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *
     * @param reviewUrl
     * The review_url
     */
    public void setReviewUrl(String reviewUrl) {
        this.reviewUrl = reviewUrl;
    }

    /**
     *
     * @return
     * The questionsUrl
     */
    public String getQuestionsUrl() {
        return questionsUrl;
    }

    public String getQuestionsUrlFrag() {
        try {
            URL url = new URL(questionsUrl);
            return url.getFile().substring(1);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     *
     * @param questionsUrl
     * The questions_url
     */
    public void setQuestionsUrl(String questionsUrl) {
        this.questionsUrl = questionsUrl;
    }

    /**
     *
     * @return
     * The correctCount
     */
    public Integer getCorrectCount() {
        return correctCount;
    }

    /**
     *
     * @param correctCount
     * The correct_count
     */
    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    /**
     *
     * @return
     * The incorrectCount
     */
    public Integer getIncorrectCount() {
        return incorrectCount;
    }

    /**
     *
     * @param incorrectCount
     * The incorrect_count
     */
    public void setIncorrectCount(Integer incorrectCount) {
        this.incorrectCount = incorrectCount;
    }

    /**
     *
     * @return
     * The lastStartedTime
     */
    public String getLastStartedTime() {
        return lastStartedTime;
    }

    /**
     *
     * @param lastStartedTime
     * The last_started_time
     */
    public void setLastStartedTime(String lastStartedTime) {
        this.lastStartedTime = lastStartedTime;
    }

    /**
     *
     * @return
     * The remainingTime
     */
    public String getRemainingTime() {
        return remainingTime;
    }

    /**
     *
     * @param remainingTime
     * The remaining_time
     */
    public void setRemainingTime(String remainingTime) {
        this.remainingTime = remainingTime;
    }

    /**
     *
     * @return
     * Time taken
     */
    public String getTimeTaken() {
        return timeTaken;
    }

    /**
     *
     * @param timeTaken
     * Time Taken
     */
    public void setTimeTaken(String timeTaken) {
        this.timeTaken = timeTaken;
    }

    /**
     *
     * @return
     * The state
     */
    public String getState() {
        return state;
    }

    /**
     *
     * @param state
     * The state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     *
     * @return
     * The percentile
     */
    public String getPercentile() {
        return percentile;
    }

    /**
     *
     * @param percentile
     * The percentile
     */
    public void setPercentile(String percentile) {
        this.percentile = percentile;
    }

}
