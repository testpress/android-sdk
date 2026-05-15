package in.testpress.store.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class InstallmentPlansResponse implements Parcelable {

    private List<InstallmentPlan> installmentPlans = new ArrayList<>();
    private List<UserInstallmentPlan> userInstallmentPlans = new ArrayList<>();

    public InstallmentPlansResponse() {}

    protected InstallmentPlansResponse(Parcel in) {
        in.readTypedList(installmentPlans, InstallmentPlan.CREATOR);
        in.readTypedList(userInstallmentPlans, UserInstallmentPlan.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(installmentPlans);
        dest.writeTypedList(userInstallmentPlans);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<InstallmentPlansResponse> CREATOR = new Creator<InstallmentPlansResponse>() {
        @Override
        public InstallmentPlansResponse createFromParcel(Parcel in) {
            return new InstallmentPlansResponse(in);
        }

        @Override
        public InstallmentPlansResponse[] newArray(int size) {
            return new InstallmentPlansResponse[size];
        }
    };

    public List<InstallmentPlan> getInstallmentPlans() {
        return installmentPlans;
    }

    public void setInstallmentPlans(List<InstallmentPlan> installmentPlans) {
        this.installmentPlans = installmentPlans;
    }

    public List<UserInstallmentPlan> getUserInstallmentPlans() {
        return userInstallmentPlans;
    }

    public void setUserInstallmentPlans(List<UserInstallmentPlan> userInstallmentPlans) {
        this.userInstallmentPlans = userInstallmentPlans;
    }

}
