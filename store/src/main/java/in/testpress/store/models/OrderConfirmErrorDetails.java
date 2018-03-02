package in.testpress.store.models;

import java.util.ArrayList;
import java.util.List;

public class OrderConfirmErrorDetails {

    private List<String> shippingAddress = new ArrayList<String>();
    private List<String> zip = new ArrayList<String>();
    private List<String> landMark = new ArrayList<String>();
    private List<String> phone = new ArrayList<String>();

    public List<String> getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(List<String> shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<String> getZip() {
        return zip;
    }

    public void setZip(List<String> zip) {
        this.zip = zip;
    }

    public List<String> getLandMark() {
        return landMark;
    }

    public void setLandMark(List<String> landMark) {
        this.landMark = landMark;
    }

    public List<String> getPhone() {
        return phone;
    }

    public void setPhone(List<String> phone) {
        this.phone = phone;
    }
}
