package in.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ProfileDetails implements Parcelable {

    private Integer id;
    private String url;
    private String username;
    private String displayName;
    private String firstName;
    private String lastName;
    private String email;
    private String photo;
    private String largeImage;
    private String mediumImage;
    private String smallImage;
    private String xSmallImage;
    private String miniImage;
    private String birthDate;
    private String gender;
    private String address1;
    private String address2;
    private String city;
    private String zip;
    private String state;
    private String stateChoices;
    private String phone;

    protected ProfileDetails(Parcel in) {
        id = in.readInt();
        url = in.readString();
        username = in.readString();
        displayName = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        email = in.readString();
        photo = in.readString();
        largeImage = in.readString();
        mediumImage = in.readString();
        smallImage = in.readString();
        xSmallImage = in.readString();
        miniImage = in.readString();
        birthDate = in.readString();
        gender = in.readString();
        address1 = in.readString();
        address2 = in.readString();
        city = in.readString();
        zip = in.readString();
        state = in.readString();
        stateChoices = in.readString();
        phone = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(url);
        dest.writeString(username);
        dest.writeString(displayName);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(email);
        dest.writeString(photo);
        dest.writeString(largeImage);
        dest.writeString(mediumImage);
        dest.writeString(smallImage);
        dest.writeString(xSmallImage);
        dest.writeString(miniImage);
        dest.writeString(birthDate);
        dest.writeString(gender);
        dest.writeString(address1);
        dest.writeString(address2);
        dest.writeString(city);
        dest.writeString(zip);
        dest.writeString(state);
        dest.writeString(stateChoices);
        dest.writeString(phone);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProfileDetails> CREATOR = new Creator<ProfileDetails>() {
        @Override
        public ProfileDetails createFromParcel(Parcel in) {
            return new ProfileDetails(in);
        }

        @Override
        public ProfileDetails[] newArray(int size) {
            return new ProfileDetails[size];
        }
    };

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
     * The username
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @param username
     * The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @return
     * The displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     *
     * @param displayName
     * The display_name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     *
     * @return
     * The firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     *
     * @param firstName
     * The first_name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     *
     * @return
     * The lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     *
     * @param lastName
     * The last_name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     *
     * @return
     * The email
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @param email
     * The email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     * @return
     * The photo
     */
    public String getPhoto() {
        return photo;
    }

    /**
     *
     * @param photo
     * The photo
     */
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    /**
     *
     * @return
     * The largeImage
     */
    public String getLargeImage() {
        return largeImage;
    }

    /**
     *
     * @param largeImage
     * The large_image
     */
    public void setLargeImage(String largeImage) {
        this.largeImage = largeImage;
    }

    /**
     *
     * @return
     * The mediumImage
     */
    public String getMediumImage() {
        return mediumImage;
    }

    /**
     *
     * @param mediumImage
     * The medium_image
     */
    public void setMediumImage(String mediumImage) {
        this.mediumImage = mediumImage;
    }

    /**
     *
     * @return
     * The smallImage
     */
    public String getSmallImage() {
        return smallImage;
    }

    /**
     *
     * @param smallImage
     * The small_image
     */
    public void setSmallImage(String smallImage) {
        this.smallImage = smallImage;
    }

    /**
     *
     * @return
     * The xSmallImage
     */
    public String getXSmallImage() {
        return xSmallImage;
    }

    /**
     *
     * @param xSmallImage
     * The x_small_image
     */
    public void setXSmallImage(String xSmallImage) {
        this.xSmallImage = xSmallImage;
    }

    /**
     *
     * @return
     * The miniImage
     */
    public String getMiniImage() {
        return miniImage;
    }

    /**
     *
     * @param miniImage
     * The mini_image
     */
    public void setMiniImage(String miniImage) {
        this.miniImage = miniImage;
    }

    /**
     *
     * @return
     * The birthDate
     */
    public String getBirthDate() {
        return birthDate;
    }

    /**
     *
     * @param birthDate
     * The birth_date
     */
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    /**
     *
     * @return
     * The gender
     */
    public String getGender() {
        return gender;
    }

    /**
     *
     * @param gender
     * The gender
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     *
     * @return
     * The address1
     */
    public String getAddress1() {
        return address1;
    }

    /**
     *
     * @param address1
     * The address1
     */
    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    /**
     *
     * @return
     * The address2
     */
    public String getAddress2() {
        return address2;
    }

    /**
     *
     * @param address2
     * The address2
     */
    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    /**
     *
     * @return
     * The city
     */
    public String getCity() {
        return city;
    }

    /**
     *
     * @param city
     * The city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     *
     * @return
     * The zip
     */
    public String getZip() {
        return zip;
    }

    /**
     *
     * @param zip
     * The zip
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     *
     * @return
     * The state
     */
    public String getState() {
        return state;
    }

    /**
     *
     * @param state
     * The state
     */
    public void setState(String state) {
        this.state = state;
    }

    public String getStateChoices() {
        return stateChoices;
    }

    public void setStateChoices(String stateChoices) {
        this.stateChoices = stateChoices;
    }

    /**
     *
     * @return
     * The phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     *
     * @param phone
     * The phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsernameOrEmail() {
        if (username != null && !username.isEmpty()){
            return username;
        }
        if (email != null && !email.isEmpty()) {
            return email;
        }
        return "null";
    }

}
