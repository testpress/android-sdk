package in.testpress.exam.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Language implements Parcelable {

    private String code;
    private String title;

    public Language(Language language) {
        code = language.getCode();
        title = language.getTitle();
    }

    protected Language(Parcel in) {
        code = in.readString();
        title = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(title);
    }

    public void update(Language language) {
        code = language.getCode();
        title = language.getTitle();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Language> CREATOR = new Creator<Language>() {
        @Override
        public Language createFromParcel(Parcel in) {
            return new Language(in);
        }

        @Override
        public Language[] newArray(int size) {
            return new Language[size];
        }
    };

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

}
