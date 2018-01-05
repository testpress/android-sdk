package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import android.os.Parcel;
import android.os.Parcelable;
// KEEP INCLUDES END

/**
 * Entity mapped to table "LANGUAGE".
 */
@Entity
public class Language implements android.os.Parcelable {

    @Id
    private Long id;
    private String code;
    private String title;
    private String exam_slug;
    private Long examId;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    @Generated
    public Language() {
    }

    public Language(Long id) {
        this.id = id;
    }

    @Generated
    public Language(Long id, String code, String title, String exam_slug, Long examId) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.exam_slug = exam_slug;
        this.examId = examId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExam_slug() {
        return exam_slug;
    }

    public void setExam_slug(String exam_slug) {
        this.exam_slug = exam_slug;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    // KEEP METHODS - put your custom methods here

    protected Language(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readLong();
        code = in.readString();
        title = in.readString();
        exam_slug = in.readString();
        examId = in.readByte() == 0x00 ? null : in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeLong(id);
        }
        dest.writeString(code);
        dest.writeString(title);
        dest.writeString(exam_slug);
        if (examId == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeLong(examId);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Language> CREATOR = new Parcelable.Creator<Language>() {
        @Override
        public Language createFromParcel(Parcel in) {
            return new Language(in);
        }

        @Override
        public Language[] newArray(int size) {
            return new Language[size];
        }
    };

    public Language(Language language) {
        code = language.getCode();
        title = language.getTitle();
    }

    public void update(Language language) {
        code = language.getCode();
        title = language.getTitle();
    }
    public Language(String code, String title) {
        this.code = code;
        this.title = title;
    }
    public Language(String code, String title, String exam_slug) {
        this.code = code;
        this.title = title;
        this.exam_slug = exam_slug;
    }
    // KEEP METHODS END

}
