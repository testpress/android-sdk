package in.testpress.exam.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Category implements Parcelable {

    private Integer id;
    private String url;
    private String name;
    private String description;
    private String slug;
    private String image;
    private String parentUrl;
    private Boolean leaf;
    private String parentSlug;
    private Integer examsCount;
    private Integer completedCount;
    private Integer availableCount;

    protected Category(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        url = in.readString();
        name = in.readString();
        description = in.readString();
        slug = in.readString();
        image = in.readString();
        parentUrl = in.readString();
        byte tmpLeaf = in.readByte();
        leaf = tmpLeaf == 0 ? null : tmpLeaf == 1;
        parentSlug = in.readString();
        if (in.readByte() == 0) {
            examsCount = null;
        } else {
            examsCount = in.readInt();
        }
        if (in.readByte() == 0) {
            completedCount = null;
        } else {
            completedCount = in.readInt();
        }
        if (in.readByte() == 0) {
            availableCount = null;
        } else {
            availableCount = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(url);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(slug);
        dest.writeString(image);
        dest.writeString(parentUrl);
        dest.writeByte((byte) (leaf == null ? 0 : leaf ? 1 : 2));
        dest.writeString(parentSlug);
        if (examsCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(examsCount);
        }
        if (completedCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(completedCount);
        }
        if (availableCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(availableCount);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
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
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The description
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     * The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     * The slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     *
     * @param slug
     * The slug
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    /**
     *
     * @return
     * The parentUrl
     */
    public String getParentUrl() {
        return parentUrl;
    }

    /**
     *
     * @param parentUrl
     * The parent_url
     */
    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }

    /**
     *
     * @return
     * The leaf
     */
    public Boolean getLeaf() {
        return leaf;
    }

    /**
     *
     * @param leaf
     * The leaf
     */
    public void setLeaf(Boolean leaf) {
        this.leaf = leaf;
    }

    /**
     *
     * @return
     * The parentSlug
     */
    public String getParentSlug() {
        return parentSlug;
    }

    /**
     *
     * @param parentSlug
     * The parent_slug
     */
    public void setParentSlug(String parentSlug) {
        this.parentSlug = parentSlug;
    }

    /**
     *
     * @return
     * The examsCount
     */
    public Integer getExamsCount() {
        return examsCount;
    }

    /**
     *
     * @param examsCount
     * The exams_count
     */
    public void setExamsCount(Integer examsCount) {
        this.examsCount = examsCount;
    }

    /**
     *
     * @return
     * The completedCount
     */
    public Integer getCompletedCount() {
        return completedCount;
    }

    /**
     *
     * @param completedCount
     * The completed_count
     */
    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    /**
     *
     * @return
     * The availableCount
     */
    public Integer getAvailableCount() {
        return availableCount;
    }

    /**
     *
     * @param availableCount
     * The available_count
     */
    public void setAvailableCount(Integer availableCount) {
        this.availableCount = availableCount;
    }

}
