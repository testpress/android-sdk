package in.testpress.store.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class InstallmentPlan implements Parcelable {

    private Integer id;
    private String price;
    private Integer numberOfInstallments;
    private Integer period;
    private String displayName;
    private List<Installment> installments = new ArrayList<>();

    public InstallmentPlan() {}

    protected InstallmentPlan(Parcel in) {
        id = in.readInt();
        price = in.readString();
        numberOfInstallments = in.readInt();
        period = in.readInt();
        displayName = in.readString();
        in.readTypedList(installments, Installment.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(price);
        dest.writeInt(numberOfInstallments);
        dest.writeInt(period);
        dest.writeString(displayName);
        dest.writeTypedList(installments);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<InstallmentPlan> CREATOR = new Creator<InstallmentPlan>() {
        @Override
        public InstallmentPlan createFromParcel(Parcel in) {
            return new InstallmentPlan(in);
        }

        @Override
        public InstallmentPlan[] newArray(int size) {
            return new InstallmentPlan[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Integer getNumberOfInstallments() {
        return numberOfInstallments;
    }

    public void setNumberOfInstallments(Integer numberOfInstallments) {
        this.numberOfInstallments = numberOfInstallments;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<Installment> getInstallments() {
        return installments;
    }

    public void setInstallments(List<Installment> installments) {
        this.installments = installments;
    }

}
