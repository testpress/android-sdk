package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END

/**
 * Entity mapped to table "BOOKMARK_FOLDER".
 */
@Entity
public class BookmarkFolder {

    @Id
    private Long id;
    private String name;
    private Integer bookmarksCount;

    // KEEP FIELDS - put your custom fields here
    public static final String UNCATEGORIZED = "Uncategorized";
    // KEEP FIELDS END

    @Generated
    public BookmarkFolder() {
    }

    public BookmarkFolder(Long id) {
        this.id = id;
    }

    @Generated
    public BookmarkFolder(Long id, String name, Integer bookmarksCount) {
        this.id = id;
        this.name = name;
        this.bookmarksCount = bookmarksCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBookmarksCount() {
        return bookmarksCount;
    }

    public void setBookmarksCount(Integer bookmarksCount) {
        this.bookmarksCount = bookmarksCount;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
