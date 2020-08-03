package in.testpress.store.models;

import com.google.gson.annotations.SerializedName;

public class PricesItem{

	@SerializedName("end_date")
	private String endDate;

	@SerializedName("price")
	private String price;

	@SerializedName("name")
	private String name;

	@SerializedName("id")
	private int id;

	@SerializedName("validity")
	private Object validity;

	@SerializedName("start_date")
	private String startDate;

	public String getEndDate(){
		return endDate;
	}

	public String getPrice(){
		return price;
	}

	public String getName(){
		return name;
	}

	public int getId(){
		return id;
	}

	public Object getValidity(){
		return validity;
	}

	public String getStartDate(){
		return startDate;
	}
}