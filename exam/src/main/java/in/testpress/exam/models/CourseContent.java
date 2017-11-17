package in.testpress.exam.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import in.testpress.models.greendao.Exam;

public class CourseContent implements Parcelable {

    private String attemptsUrl;
    private Exam exam;

    public CourseContent(String attemptsUrl, Exam exam) {
        this.attemptsUrl = attemptsUrl;
        this.exam = exam;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(attemptsUrl);
        dest.writeLong(exam.getId());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CourseContent> CREATOR = new Creator<CourseContent>() {
        @Override
        public CourseContent createFromParcel(Parcel in) {
            return new CourseContent(in);
        }

        @Override
        public CourseContent[] newArray(int size) {
            return new CourseContent[size];
        }
    };

    protected CourseContent(Parcel in) {
        attemptsUrl = in.readString();
        exam = in.readParcelable(Exam.class.getClassLoader());
    }

    public String getAttemptsUrl() {
        return attemptsUrl;
    }

    public void setAttemptsUrl(String attemptsUrl) {
        this.attemptsUrl = attemptsUrl;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }
}
