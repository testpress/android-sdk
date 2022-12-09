package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

import in.testpress.util.IntegerList;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import android.content.Context;

import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
// KEEP INCLUDES END

/**
 * Entity mapped to table "PRODUCT".
 */
@Entity
public class Product {

    @Id
    private Long id;
    private String title;
    private String slug;
    private String descriptionHtml;
    private String image;
    private String startDate;
    private String endDate;
    private String buyNowText;
    private String surl;
    private String furl;
    private String currentPrice;

    @Convert(converter = in.testpress.util.IntegerListConverter.class, columnType = String.class)
    private IntegerList prices;

    @SerializedName("courses")
    @Convert(converter = in.testpress.util.IntegerListConverter.class, columnType = String.class)
    private IntegerList courseIds;
    private Long order;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    @Generated
    public Product() {
    }

    public Product(Long id) {
        this.id = id;
    }

    @Generated
    public Product(Long id, String title, String slug, String descriptionHtml, String image, String startDate, String endDate, String buyNowText, String surl, String furl, String currentPrice, IntegerList prices, IntegerList courseIds, Long order) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.descriptionHtml = descriptionHtml;
        this.image = image;
        this.startDate = startDate;
        this.endDate = endDate;
        this.buyNowText = buyNowText;
        this.surl = surl;
        this.furl = furl;
        this.currentPrice = currentPrice;
        this.prices = prices;
        this.courseIds = courseIds;
        this.order = order;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescriptionHtml() {
        return descriptionHtml;
    }

    public void setDescriptionHtml(String descriptionHtml) {
        this.descriptionHtml = descriptionHtml;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getBuyNowText() {
        return buyNowText;
    }

    public void setBuyNowText(String buyNowText) {
        this.buyNowText = buyNowText;
    }

    public String getSurl() {
        return surl;
    }

    public void setSurl(String surl) {
        this.surl = surl;
    }

    public String getFurl() {
        return furl;
    }

    public void setFurl(String furl) {
        this.furl = furl;
    }

    public String getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(String currentPrice) {
        this.currentPrice = currentPrice;
    }

    public IntegerList getPrices() {
        return prices;
    }

    public void setPrices(IntegerList prices) {
        this.prices = prices;
    }

    public IntegerList getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(IntegerList courseIds) {
        this.courseIds = courseIds;
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    // KEEP METHODS - put your custom methods here

    public static QueryBuilder<Product> getQueryBuilder(Context context) {
        ProductDao bookmarkDao = TestpressSDKDatabase.getProductDao(context);
        QueryBuilder<Product> queryBuilder = bookmarkDao.queryBuilder();
        return queryBuilder;
    }
    // KEEP METHODS END

}
