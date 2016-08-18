package in.testpress.exam.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReviewItem implements Parcelable {

    private Integer id;
    private String url;
    private ReviewQuestion question;
    private List<Integer> selectedAnswers = new ArrayList<Integer>();
    private Boolean review;
    private HashMap<String, Bitmap> images = new HashMap<>();
    private ArrayList<String> imageUrl = new ArrayList<>();

    ReviewItem() {
        selectedAnswers = new ArrayList<Integer>();
    }

    // Parcelling part
    public ReviewItem(Parcel parcel) {
        id = parcel.readInt();
        question = parcel.readParcelable(ReviewQuestion.class.getClassLoader());
        url = parcel.readString();
        selectedAnswers = new ArrayList<Integer>();
        parcel.readList(selectedAnswers, List.class.getClassLoader());
        review = parcel.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeParcelable(question, i);
        parcel.writeString(url);
        parcel.writeList(selectedAnswers);
        if (review == null) {
            parcel.writeByte((byte) (0));
        } else {
            parcel.writeByte((byte) (review ? 1 : 0)); //if review == true, byte == 1
        }
    }

    public static final Creator CREATOR = new Creator() {
        public ReviewItem createFromParcel(Parcel parcel) {
            return new ReviewItem(parcel);
        }

        public ReviewItem[] newArray(int size) {
            return new ReviewItem[size];
        }
    };

    public void setImages(String url, Bitmap image) {
        this.images.put(url, image);
    }

    public HashMap<String, Bitmap> getImages() {
        return images;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl.add(imageUrl);
    }

    public ArrayList<String> getImageUrl() {
        return imageUrl;
    }

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     * The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return
     * The question
     */
    public ReviewQuestion getReviewQuestion() {
        return question;
    }

    /**
     *
     * @param reviewQuestion
     * The question
     */
    public void setReviewQuestion(ReviewQuestion reviewQuestion) {
        this.question = reviewQuestion;
    }

    /**
     *
     * @return
     * The selectedAnswers
     */
    public List<Integer> getSelectedAnswers() {
        return selectedAnswers;
    }

    /**
     *
     * @param selectedAnswers
     * The selected_answers
     */
    public void setSelectedAnswers(List<Integer> selectedAnswers) {
        this.selectedAnswers = selectedAnswers;
    }

    /**
     *
     * @return
     * The review
     */
    public Boolean getReview() {
        return review;
    }

    /**
     *
     * @param review
     * The review
     */
    public void setReview(Boolean review) {
        this.review = review;
    }

}