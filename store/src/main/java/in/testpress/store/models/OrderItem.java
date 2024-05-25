package in.testpress.store.models;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderItem implements Parcelable {

    private String product;
    private Integer quantity;
    private String price;
    private Integer priceId;
    private String productSlug;

    public OrderItem(){}

    // Parcelling part
    public OrderItem(Parcel parcel){
        product  = parcel.readString();
        price    = parcel.readString();
        quantity = parcel.readInt();
        priceId = parcel.readInt();
        productSlug = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(product);
        parcel.writeString(price);
        parcel.writeInt(quantity);
        parcel.writeInt(priceId);
        parcel.writeString(productSlug);
    }

    public static final Creator<OrderItem> CREATOR = new Creator<OrderItem>() {
        public OrderItem createFromParcel(Parcel in) {
            return new OrderItem(in);
        }

        public OrderItem[] newArray(int size) {
            return new OrderItem[size];
        }
    };

    /**
     *
     * @return
     * The product
     */
    public String getProduct() {
        return product;
    }

    /**
     *
     * @param product
     * The product
     */
    public void setProduct(String product) {
        this.product = product;
    }

    /**
     *
     * @return
     * The quantity
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     *
     * @param quantity
     * The quantity
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     *
     * @return
     * The price
     */
    public String getPrice() {
        return price;
    }

    /**
     *
     * @param price
     * The price
     */
    public void setPrice(String price) {
        this.price = price;
    }

    /**
     *
     * @return
     * The priceId
     */
    public Integer getPriceId() {
        return priceId;
    }

    /**
     *
     * @param priceId
     * The priceId
     */
    public void setPriceId(Integer priceId) {
        this.priceId = priceId;
    }

    /**
     *
     * @return
     * The productSlug
     */
    public String getProductSlug() {
        return productSlug;
    }

    /**
     *
     * @param productSlug
     * The productSlug
     */
    public void setProductSlug(String productSlug) {
        this.productSlug = productSlug;
    }

}