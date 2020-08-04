package in.testpress.store.models;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class ProductDetailResponse {

	@SerializedName("end_date")
	private Object endDate;

	@SerializedName("image")
	private String image;

	@SerializedName("courses")
	private List<CoursesItem> courses;

	@SerializedName("surl")
	private Object surl;

	@SerializedName("title")
	private String title;

	@SerializedName("payment_link")
	private String paymentLink;

	@SerializedName("buy_now_text")
	private String buyNowText;

	@SerializedName("furl")
	private Object furl;

	@SerializedName("id")
	private int id;

	@SerializedName("description_html")
	private String descriptionHtml;

	@SerializedName("current_price")
	private String currentPrice;

	@SerializedName("prices")
	private List<PricesItem> prices;

	@SerializedName("slug")
	private String slug;

	@SerializedName("start_date")
	private Object startDate;

	@SerializedName("order")
	private OrderDetail orderDetail;

	public Object getEndDate(){
		return endDate;
	}

	public String getImage(){
		return image;
	}

	public List<CoursesItem> getCourses(){
		return courses;
	}

	public Object getSurl(){
		return surl;
	}

	public String getTitle(){
		return title;
	}

	public String getPaymentLink(){
		return paymentLink;
	}

	public String getBuyNowText(){
		return buyNowText;
	}

	public Object getFurl(){
		return furl;
	}

	public int getId(){
		return id;
	}

	public String getDescriptionHtml(){
		return descriptionHtml;
	}

	public String getCurrentPrice(){
		return currentPrice;
	}

	public List<PricesItem> getPrices(){
		return prices;
	}

	public String getSlug(){
		return slug;
	}

	public Object getStartDate(){
		return startDate;
	}

	public OrderDetail getOrder(){
		return orderDetail;
	}
}