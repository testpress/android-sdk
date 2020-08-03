package in.testpress.store.models;

import com.google.gson.annotations.SerializedName;

public class OrderItemsItem{

	@SerializedName("product")
	private String product;

	@SerializedName("quantity")
	private int quantity;

	@SerializedName("price")
	private String price;

	public String getProduct(){
		return product;
	}

	public int getQuantity(){
		return quantity;
	}

	public String getPrice(){
		return price;
	}
}