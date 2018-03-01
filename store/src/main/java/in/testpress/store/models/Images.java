package in.testpress.store.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Images implements Parcelable {
    private String original;
    private String medium;
    private String small;

    // Parcelling part
    public Images(Parcel parcel){
        original = parcel.readString();
        medium   = parcel.readString();
        small    = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(original);
        parcel.writeString(medium);
        parcel.writeString(small);
    }

    public static final Creator<Images> CREATOR = new Creator<Images>() {
        public Images createFromParcel(Parcel in) {
            return new Images(in);
        }

        public Images[] newArray(int size) {
            return new Images[size];
        }
    };

    public String getOriginal() { return original; }
    public void setOriginal(String original) { this.original = original; }

    public String getMedium() { return medium; }
    public void setMedium(String medium) { this.medium = medium; }

    public String getSmall() { return small; }
    public void setSmall(String small) { this.small = small; }
}
