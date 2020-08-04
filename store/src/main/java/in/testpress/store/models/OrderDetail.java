package in.testpress.store.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderDetail {
        @SerializedName("date")
        private String date;

        @SerializedName("voucher")
        private Voucher voucher;

        @SerializedName("amount_without_discounts")
        private String amountWithoutDiscounts;

        @SerializedName("access_code")
        private String accessCode;

        @SerializedName("product_info")
        private String productInfo;

        @SerializedName("pg_url")
        private String pgUrl;

        @SerializedName("land_mark")
        private String landMark;

        @SerializedName("amount_without_processing_fee")
        private String amountWithoutProcessingFee;

        @SerializedName("checksum")
        private String checksum;

        @SerializedName("id")
        private int id;

        @SerializedName("shipping_address")
        private String shippingAddress;

        @SerializedName("email")
        private String email;

        @SerializedName("order_items")
        private List<OrderItemsItem> orderItems;

        @SerializedName("zip")
        private String zip;

        @SerializedName("amount")
        private String amount;

        @SerializedName("apikey")
        private String apikey;

        @SerializedName("ip_address")
        private String ipAddress;

        @SerializedName("enc_data")
        private Object encData;

        @SerializedName("phone")
        private String phone;

        @SerializedName("name")
        private String name;

        @SerializedName("uses_testpress_pg")
        private boolean usesTestpressPg;

        @SerializedName("order_id")
        private String orderId;

        @SerializedName("user")
        private int user;

        @SerializedName("mobile_sdk_hash")
        private String mobileSdkHash;

        @SerializedName("status")
        private String status;

        public String getDate(){
            return date;
        }

        public Voucher getVoucher(){
            return voucher;
        }

        public String getAmountWithoutDiscounts(){
            return amountWithoutDiscounts;
        }

        public String getAccessCode(){
            return accessCode;
        }

        public String getProductInfo(){
            return productInfo;
        }

        public String getPgUrl(){
            return pgUrl;
        }

        public String getLandMark(){
            return landMark;
        }

        public String getAmountWithoutProcessingFee(){
            return amountWithoutProcessingFee;
        }

        public String getChecksum(){
            return checksum;
        }

        public int getId(){
            return id;
        }

        public String getShippingAddress(){
            return shippingAddress;
        }

        public String getEmail(){
            return email;
        }

        public List<OrderItemsItem> getOrderItems(){
            return orderItems;
        }

        public String getZip(){
            return zip;
        }

        public String getAmount(){
            return amount;
        }

        public String getApikey(){
            return apikey;
        }

        public String getIpAddress(){
            return ipAddress;
        }

        public Object getEncData(){
            return encData;
        }

        public String getPhone(){
            return phone;
        }

        public String getName(){
            return name;
        }

        public boolean isUsesTestpressPg(){
            return usesTestpressPg;
        }

        public String getOrderId(){
            return orderId;
        }

        public int getUser(){
            return user;
        }

        public String getMobileSdkHash(){
            return mobileSdkHash;
        }

        public String getStatus(){
            return status;
        }
}
