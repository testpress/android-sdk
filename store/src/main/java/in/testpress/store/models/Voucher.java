package in.testpress.store.models;

import com.google.gson.annotations.SerializedName;

public class Voucher{

	@SerializedName("code")
	private String code;

	public String getCode(){
		return code;
	}
}