package in.testpress.exam.models;

import android.os.Parcel;
import android.os.Parcelable;

public class EssayTopic implements Parcelable {
    private Long id;
    private String title;

    protected EssayTopic(Parcel in) {
        id = in.readLong();
        title = in.readString();
    }

    public static final Creator<EssayTopic> CREATOR = new Creator<EssayTopic>() {
        @Override
        public EssayTopic createFromParcel(Parcel in) {
            return new EssayTopic(in);
        }

        @Override
        public EssayTopic[] newArray(int size) {
            return new EssayTopic[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
