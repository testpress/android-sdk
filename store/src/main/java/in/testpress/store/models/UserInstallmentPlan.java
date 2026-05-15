package in.testpress.store.models;

import android.os.Parcel;
import android.os.Parcelable;

public class UserInstallmentPlan implements Parcelable {

    private Integer id;
    private String status;
    private Integer paidInstallmentCount;
    private Integer totalInstallments;
    private String nextDueAmount;
    private String nextDueDate;
    private String amountPaid;
    private String amountDue;
    private Integer installmentPlanId;
    private Boolean isCustomPlan;
    private Boolean isPaid;
    private Boolean isPartiallyPaid;
    private Boolean isOverdue;

    public UserInstallmentPlan() {}

    protected UserInstallmentPlan(Parcel in) {
        id = in.readInt();
        status = in.readString();
        paidInstallmentCount = in.readInt();
        totalInstallments = in.readInt();
        nextDueAmount = in.readString();
        nextDueDate = in.readString();
        amountPaid = in.readString();
        amountDue = in.readString();
        installmentPlanId = in.readInt();
        isCustomPlan = in.readByte() != 0;
        isPaid = in.readByte() != 0;
        isPartiallyPaid = in.readByte() != 0;
        isOverdue = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(status);
        dest.writeInt(paidInstallmentCount);
        dest.writeInt(totalInstallments);
        dest.writeString(nextDueAmount);
        dest.writeString(nextDueDate);
        dest.writeString(amountPaid);
        dest.writeString(amountDue);
        dest.writeInt(installmentPlanId);
        dest.writeByte((byte) (isCustomPlan ? 1 : 0));
        dest.writeByte((byte) (isPaid ? 1 : 0));
        dest.writeByte((byte) (isPartiallyPaid ? 1 : 0));
        dest.writeByte((byte) (isOverdue ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserInstallmentPlan> CREATOR = new Creator<UserInstallmentPlan>() {
        @Override
        public UserInstallmentPlan createFromParcel(Parcel in) {
            return new UserInstallmentPlan(in);
        }

        @Override
        public UserInstallmentPlan[] newArray(int size) {
            return new UserInstallmentPlan[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPaidInstallmentCount() {
        return paidInstallmentCount;
    }

    public void setPaidInstallmentCount(Integer paidInstallmentCount) {
        this.paidInstallmentCount = paidInstallmentCount;
    }

    public Integer getTotalInstallments() {
        return totalInstallments;
    }

    public void setTotalInstallments(Integer totalInstallments) {
        this.totalInstallments = totalInstallments;
    }

    public String getNextDueAmount() {
        return nextDueAmount;
    }

    public void setNextDueAmount(String nextDueAmount) {
        this.nextDueAmount = nextDueAmount;
    }

    public String getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(String nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public String getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(String amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(String amountDue) {
        this.amountDue = amountDue;
    }

    public Integer getInstallmentPlanId() {
        return installmentPlanId;
    }

    public void setInstallmentPlanId(Integer installmentPlanId) {
        this.installmentPlanId = installmentPlanId;
    }

    public Boolean getIsCustomPlan() {
        return isCustomPlan;
    }

    public void setIsCustomPlan(Boolean isCustomPlan) {
        this.isCustomPlan = isCustomPlan;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public Boolean getIsPartiallyPaid() {
        return isPartiallyPaid;
    }

    public void setIsPartiallyPaid(Boolean isPartiallyPaid) {
        this.isPartiallyPaid = isPartiallyPaid;
    }

    public Boolean getIsOverdue() {
        return isOverdue;
    }

    public void setIsOverdue(Boolean isOverdue) {
        this.isOverdue = isOverdue;
    }

}
