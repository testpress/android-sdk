package in.testpress.exam.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Permission implements Parcelable {

    private boolean hasPermission;
    private String nextRetakeTime;

    protected Permission(Parcel in) {
        hasPermission = in.readByte() != 0;
        nextRetakeTime = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (hasPermission ? 1 : 0));
        dest.writeString(nextRetakeTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Permission> CREATOR = new Creator<Permission>() {
        @Override
        public Permission createFromParcel(Parcel in) {
            return new Permission(in);
        }

        @Override
        public Permission[] newArray(int size) {
            return new Permission[size];
        }
    };

    public Boolean getHasPermission() {
        return hasPermission;
    }

    public void setHasPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }

    public String getNextRetakeTime() {
        return nextRetakeTime;
    }

    public void setNextRetakeTime(String nextRetakeTime) {
        this.nextRetakeTime = nextRetakeTime;
    }

}