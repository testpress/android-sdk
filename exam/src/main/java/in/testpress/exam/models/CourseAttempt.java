package in.testpress.exam.models;

import android.os.Parcel;
import android.os.Parcelable;

import static in.testpress.exam.network.TestpressExamApiClient.CONTENT_ATTEMPTS_PATH;
import static in.testpress.exam.network.TestpressExamApiClient.END_EXAM_PATH;

public class CourseAttempt implements Parcelable {

    private Integer id;
    private String type;
    private Integer objectId;
    private String objectUrl;
    private String trophies;
    private Attempt assessment;
    private CourseContent chapterContent;

    protected CourseAttempt(Parcel in) {
        id = in.readInt();
        type = in.readString();
        objectUrl = in.readString();
        trophies = in.readString();
        assessment = in.readParcelable(Attempt.class.getClassLoader());
        chapterContent = in.readParcelable(CourseContent.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(type);
        dest.writeString(objectUrl);
        dest.writeString(trophies);
        dest.writeParcelable(assessment, flags);
        dest.writeParcelable(chapterContent, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CourseAttempt> CREATOR = new Creator<CourseAttempt>() {
        @Override
        public CourseAttempt createFromParcel(Parcel in) {
            return new CourseAttempt(in);
        }

        @Override
        public CourseAttempt[] newArray(int size) {
            return new CourseAttempt[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getObjectId() {
        return objectId;
    }

    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    public String getObjectUrl() {
        return objectUrl;
    }

    public void setObjectUrl(String objectUrl) {
        this.objectUrl = objectUrl;
    }

    public String getTrophies() {
        return trophies;
    }

    public void setTrophies(String trophies) {
        this.trophies = trophies;
    }

    public Attempt getAssessment() {
        return assessment;
    }

    public void setAssessment(Attempt assessment) {
        this.assessment = assessment;
    }

    public CourseContent getChapterContent() {
        return chapterContent;
    }

    public void setChapterContent(CourseContent chapterContent) {
        this.chapterContent = chapterContent;
    }

    public String getEndAttemptUrl() {
        return CONTENT_ATTEMPTS_PATH + id + END_EXAM_PATH;
    }
}
