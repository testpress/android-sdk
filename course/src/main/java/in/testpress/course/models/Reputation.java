package in.testpress.course.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import in.testpress.exam.models.ProfileDetails;

public class Reputation implements Parcelable {

    private Integer id;
    private ProfileDetails user;
    private Integer trophiesCount;
    private Integer rank;
    private Integer difference;

    protected Reputation(Parcel in) {
        id = in.readInt();
        user = in.readParcelable(ProfileDetails.class.getClassLoader());
        trophiesCount = in.readInt();
        rank = in.readInt();
        String difference = in.readString();
        this.difference = TextUtils.isEmpty(difference) ? null : Integer.parseInt(difference);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeParcelable(user, flags);
        dest.writeInt(trophiesCount);
        dest.writeInt(rank);
        dest.writeString(difference + "");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Reputation> CREATOR = new Creator<Reputation>() {
        @Override
        public Reputation createFromParcel(Parcel in) {
            return new Reputation(in);
        }

        @Override
        public Reputation[] newArray(int size) {
            return new Reputation[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ProfileDetails getUser() {
        return user;
    }

    public void setUser(ProfileDetails user) {
        this.user = user;
    }

    public Integer getTrophiesCount() {
        return trophiesCount;
    }

    public void setTrophiesCount(Integer trophiesCount) {
        this.trophiesCount = trophiesCount;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getDifference() {
        return difference;
    }

    public void setDifference(Integer difference) {
        this.difference = difference;
    }

}
