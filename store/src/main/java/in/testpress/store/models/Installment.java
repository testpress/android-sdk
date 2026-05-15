package in.testpress.store.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Installment implements Parcelable {

    private Integer id;
    private Integer order;
    private String price;
    private Boolean isPaid;
    private String paidOn;
    private Boolean isCurrentInstallment;
    private String dueDate;

    public Installment() {}

    protected Installment(Parcel in) {
        id = in.readInt();
        order = in.readInt();
        price = in.readString();
        isPaid = in.readByte() != 0;
        paidOn = in.readString();
        isCurrentInstallment = in.readByte() != 0;
        dueDate = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(order);
        dest.writeString(price);
        dest.writeByte((byte) (isPaid ? 1 : 0));
        dest.writeString(paidOn);
        dest.writeByte((byte) (isCurrentInstallment ? 1 : 0));
        dest.writeString(dueDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Installment> CREATOR = new Creator<Installment>() {
        @Override
        public Installment createFromParcel(Parcel in) {
            return new Installment(in);
        }

        @Override
        public Installment[] newArray(int size) {
            return new Installment[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public String getPaidOn() {
        return paidOn;
    }

    public void setPaidOn(String paidOn) {
        this.paidOn = paidOn;
    }

    public Boolean getIsCurrentInstallment() {
        return isCurrentInstallment;
    }

    public void setIsCurrentInstallment(Boolean isCurrentInstallment) {
        this.isCurrentInstallment = isCurrentInstallment;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

}
