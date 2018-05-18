package in.testpress.models.greendao;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END

/**
 * Entity mapped to table "DIRECTION".
 */
@Entity
public class Direction {

    @Id
    private Long id;
    private String html;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    @Generated
    public Direction() {
    }

    public Direction(Long id) {
        this.id = id;
    }

    @Generated
    public Direction(Long id, String html) {
        this.id = id;
        this.html = html;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
