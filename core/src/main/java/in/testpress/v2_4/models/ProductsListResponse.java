package in.testpress.v2_4.models;

import java.util.ArrayList;
import java.util.List;

import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.Price;
import in.testpress.models.greendao.Product;

public class ProductsListResponse {
    private List<Product> products = new ArrayList<>();
    private List<Price> prices = new ArrayList<>();
    private List<Course> courses = new ArrayList<>();


    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(
            List<Product> products) {
        this.products = products;
    }


    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<Price> getPrices() {
        return prices;
    }

    public void setPrices(List<Price> prices) {
        this.prices = prices;
    }

}
