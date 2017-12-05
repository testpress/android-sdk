package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import android.os.Parcel;
import android.os.Parcelable;
// KEEP INCLUDES END

/**
 * Entity mapped to table "ATTACHMENT".
 */
@Entity
public class Attachment implements android.os.Parcelable {
    private String title;
    private String attachmentUrl;
    private String description;

    @Id
    private Long id;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    @Generated
    public Attachment() {
    }

    public Attachment(Long id) {
        this.id = id;
    }

    @Generated
    public Attachment(String title, String attachmentUrl, String description, Long id) {
        this.title = title;
        this.attachmentUrl = attachmentUrl;
        this.description = description;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // KEEP METHODS - put your custom methods here
    protected Attachment(Parcel in) {
        title = in.readString();
        attachmentUrl = in.readString();
        description = in.readString();
        id = in.readByte() == 0x00 ? null : in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(attachmentUrl);
        dest.writeString(description);
        if (id == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeLong(id);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Attachment> CREATOR = new Parcelable.Creator<Attachment>() {
        @Override
        public Attachment createFromParcel(Parcel in) {
            return new Attachment(in);
        }

        @Override
        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };
    // KEEP METHODS END

}
