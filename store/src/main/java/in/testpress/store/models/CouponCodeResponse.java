package in.testpress.store.models;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class CouponCodeResponse {

	private String date;
	private Voucher voucher;
	private String amountWithoutDiscounts;
	private String accessCode;
	private String productInfo;
	private String pgUrl;
	private String landMark;
	private String amountWithoutProcessingFee;
	private String checksum;
	private int id;
	private String shippingAddress;
	private String email;
	private String status;
	private String zip;
	private String amount;
	private String apikey;
	private String phone;
	private String name;
	private int user;

	@SerializedName("order_items")
	private List<OrderItemsItem> orderItems;

	@SerializedName("ip_address")
	private String ipAddress;

	@SerializedName("enc_data")
	private Object encData;

	@SerializedName("uses_testpress_pg")
	private boolean usesTestpressPg;

	@SerializedName("order_id")
	private String orderId;

	@SerializedName("mobile_sdk_hash")
	private String mobileSdkHash;

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