package in.testpress.exam.models;

import android.os.Parcel;
import android.os.Parcelable;

public class AttemptAnswer implements Parcelable {

    protected String textHtml;
    protected Integer id;

    public AttemptAnswer() {}

    // Parcelling part
    public AttemptAnswer(Parcel parcel){
        textHtml = parcel.readString();
        id = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(textHtml);
        parcel.writeInt(id);
    }

    public static final Creator CREATOR = new Creator() {
        public AttemptAnswer createFromParcel(Parcel parcel) {
            return new AttemptAnswer(parcel);
        }

        public AttemptAnswer[] newArray(int size) {
            return new AttemptAnswer[size];
        }
    };

    /**
     *
     * @return
     * The textHtml
     */
    public String getTextHtml() {
        return textHtml;
    }

    /**
     *
     * @param textHtml
     * The text_html
     */
    public void setTextHtml(String textHtml) {
        this.textHtml = textHtml;
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

}
