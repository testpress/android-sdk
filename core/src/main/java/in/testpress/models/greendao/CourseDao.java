package in.testpress.models.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "COURSE".
*/
public class CourseDao extends AbstractDao<Course, Long> {

    public static final String TABLENAME = "COURSE";

    /**
     * Properties of entity Course.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "ID");
        public final static Property Url = new Property(1, String.class, "url", false, "URL");
        public final static Property Title = new Property(2, String.class, "title", false, "TITLE");
        public final static Property Description = new Property(3, String.class, "description", false, "DESCRIPTION");
        public final static Property Image = new Property(4, String.class, "image", false, "IMAGE");
        public final static Property Modified = new Property(5, String.class, "modified", false, "MODIFIED");
        public final static Property ModifiedDate = new Property(6, Long.class, "modifiedDate", false, "MODIFIED_DATE");
        public final static Property ContentsUrl = new Property(7, String.class, "contentsUrl", false, "CONTENTS_URL");
        public final static Property ChaptersUrl = new Property(8, String.class, "chaptersUrl", false, "CHAPTERS_URL");
        public final static Property Slug = new Property(9, String.class, "slug", false, "SLUG");
        public final static Property TrophiesCount = new Property(10, Integer.class, "trophiesCount", false, "TROPHIES_COUNT");
        public final static Property ChaptersCount = new Property(11, Integer.class, "chaptersCount", false, "CHAPTERS_COUNT");
        public final static Property ContentsCount = new Property(12, Integer.class, "contentsCount", false, "CONTENTS_COUNT");
        public final static Property Order = new Property(13, Integer.class, "order", false, "ORDER");
        public final static Property Active = new Property(14, Boolean.class, "active", false, "ACTIVE");
        public final static Property External_content_link = new Property(15, String.class, "external_content_link", false, "EXTERNAL_CONTENT_LINK");
        public final static Property External_link_label = new Property(16, String.class, "external_link_label", false, "EXTERNAL_LINK_LABEL");
        public final static Property ChildItemsLoaded = new Property(17, boolean.class, "childItemsLoaded", false, "CHILD_ITEMS_LOADED");
        public final static Property IsProduct = new Property(18, Boolean.class, "isProduct", false, "IS_PRODUCT");
        public final static Property IsMyCourse = new Property(19, Boolean.class, "isMyCourse", false, "IS_MY_COURSE");
    }

    private DaoSession daoSession;


    public CourseDao(DaoConfig config) {
        super(config);
    }
    
    public CourseDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"COURSE\" (" + //
                "\"ID\" INTEGER PRIMARY KEY ," + // 0: id
                "\"URL\" TEXT," + // 1: url
                "\"TITLE\" TEXT," + // 2: title
                "\"DESCRIPTION\" TEXT," + // 3: description
                "\"IMAGE\" TEXT," + // 4: image
                "\"MODIFIED\" TEXT," + // 5: modified
                "\"MODIFIED_DATE\" INTEGER," + // 6: modifiedDate
                "\"CONTENTS_URL\" TEXT," + // 7: contentsUrl
                "\"CHAPTERS_URL\" TEXT," + // 8: chaptersUrl
                "\"SLUG\" TEXT," + // 9: slug
                "\"TROPHIES_COUNT\" INTEGER," + // 10: trophiesCount
                "\"CHAPTERS_COUNT\" INTEGER," + // 11: chaptersCount
                "\"CONTENTS_COUNT\" INTEGER," + // 12: contentsCount
                "\"ORDER\" INTEGER," + // 13: order
                "\"ACTIVE\" INTEGER," + // 14: active
                "\"EXTERNAL_CONTENT_LINK\" TEXT," + // 15: external_content_link
                "\"EXTERNAL_LINK_LABEL\" TEXT," + // 16: external_link_label
                "\"CHILD_ITEMS_LOADED\" INTEGER NOT NULL ," + // 17: childItemsLoaded
                "\"IS_PRODUCT\" INTEGER," + // 18: isProduct
                "\"IS_MY_COURSE\" INTEGER);"); // 19: isMyCourse
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"COURSE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Course entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(2, url);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(3, title);
        }
 
        String description = entity.getDescription();
        if (description != null) {
            stmt.bindString(4, description);
        }
 
        String image = entity.getImage();
        if (image != null) {
            stmt.bindString(5, image);
        }
 
        String modified = entity.getModified();
        if (modified != null) {
            stmt.bindString(6, modified);
        }
 
        Long modifiedDate = entity.getModifiedDate();
        if (modifiedDate != null) {
            stmt.bindLong(7, modifiedDate);
        }
 
        String contentsUrl = entity.getContentsUrl();
        if (contentsUrl != null) {
            stmt.bindString(8, contentsUrl);
        }
 
        String chaptersUrl = entity.getChaptersUrl();
        if (chaptersUrl != null) {
            stmt.bindString(9, chaptersUrl);
        }
 
        String slug = entity.getSlug();
        if (slug != null) {
            stmt.bindString(10, slug);
        }
 
        Integer trophiesCount = entity.getTrophiesCount();
        if (trophiesCount != null) {
            stmt.bindLong(11, trophiesCount);
        }
 
        Integer chaptersCount = entity.getChaptersCount();
        if (chaptersCount != null) {
            stmt.bindLong(12, chaptersCount);
        }
 
        Integer contentsCount = entity.getContentsCount();
        if (contentsCount != null) {
            stmt.bindLong(13, contentsCount);
        }
 
        Integer order = entity.getOrder();
        if (order != null) {
            stmt.bindLong(14, order);
        }
 
        Boolean active = entity.getActive();
        if (active != null) {
            stmt.bindLong(15, active ? 1L: 0L);
        }
 
        String external_content_link = entity.getExternal_content_link();
        if (external_content_link != null) {
            stmt.bindString(16, external_content_link);
        }
 
        String external_link_label = entity.getExternal_link_label();
        if (external_link_label != null) {
            stmt.bindString(17, external_link_label);
        }
        stmt.bindLong(18, entity.getChildItemsLoaded() ? 1L: 0L);
 
        Boolean isProduct = entity.getIsProduct();
        if (isProduct != null) {
            stmt.bindLong(19, isProduct ? 1L: 0L);
        }
 
        Boolean isMyCourse = entity.getIsMyCourse();
        if (isMyCourse != null) {
            stmt.bindLong(20, isMyCourse ? 1L: 0L);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Course entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(2, url);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(3, title);
        }
 
        String description = entity.getDescription();
        if (description != null) {
            stmt.bindString(4, description);
        }
 
        String image = entity.getImage();
        if (image != null) {
            stmt.bindString(5, image);
        }
 
        String modified = entity.getModified();
        if (modified != null) {
            stmt.bindString(6, modified);
        }
 
        Long modifiedDate = entity.getModifiedDate();
        if (modifiedDate != null) {
            stmt.bindLong(7, modifiedDate);
        }
 
        String contentsUrl = entity.getContentsUrl();
        if (contentsUrl != null) {
            stmt.bindString(8, contentsUrl);
        }
 
        String chaptersUrl = entity.getChaptersUrl();
        if (chaptersUrl != null) {
            stmt.bindString(9, chaptersUrl);
        }
 
        String slug = entity.getSlug();
        if (slug != null) {
            stmt.bindString(10, slug);
        }
 
        Integer trophiesCount = entity.getTrophiesCount();
        if (trophiesCount != null) {
            stmt.bindLong(11, trophiesCount);
        }
 
        Integer chaptersCount = entity.getChaptersCount();
        if (chaptersCount != null) {
            stmt.bindLong(12, chaptersCount);
        }
 
        Integer contentsCount = entity.getContentsCount();
        if (contentsCount != null) {
            stmt.bindLong(13, contentsCount);
        }
 
        Integer order = entity.getOrder();
        if (order != null) {
            stmt.bindLong(14, order);
        }
 
        Boolean active = entity.getActive();
        if (active != null) {
            stmt.bindLong(15, active ? 1L: 0L);
        }
 
        String external_content_link = entity.getExternal_content_link();
        if (external_content_link != null) {
            stmt.bindString(16, external_content_link);
        }
 
        String external_link_label = entity.getExternal_link_label();
        if (external_link_label != null) {
            stmt.bindString(17, external_link_label);
        }
        stmt.bindLong(18, entity.getChildItemsLoaded() ? 1L: 0L);
 
        Boolean isProduct = entity.getIsProduct();
        if (isProduct != null) {
            stmt.bindLong(19, isProduct ? 1L: 0L);
        }
 
        Boolean isMyCourse = entity.getIsMyCourse();
        if (isMyCourse != null) {
            stmt.bindLong(20, isMyCourse ? 1L: 0L);
        }
    }

    @Override
    protected final void attachEntity(Course entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Course readEntity(Cursor cursor, int offset) {
        Course entity = new Course( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // url
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // title
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // description
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // image
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // modified
            cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6), // modifiedDate
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // contentsUrl
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // chaptersUrl
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // slug
            cursor.isNull(offset + 10) ? null : cursor.getInt(offset + 10), // trophiesCount
            cursor.isNull(offset + 11) ? null : cursor.getInt(offset + 11), // chaptersCount
            cursor.isNull(offset + 12) ? null : cursor.getInt(offset + 12), // contentsCount
            cursor.isNull(offset + 13) ? null : cursor.getInt(offset + 13), // order
            cursor.isNull(offset + 14) ? null : cursor.getShort(offset + 14) != 0, // active
            cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15), // external_content_link
            cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16), // external_link_label
            cursor.getShort(offset + 17) != 0, // childItemsLoaded
            cursor.isNull(offset + 18) ? null : cursor.getShort(offset + 18) != 0, // isProduct
            cursor.isNull(offset + 19) ? null : cursor.getShort(offset + 19) != 0 // isMyCourse
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Course entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUrl(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setTitle(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setDescription(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setImage(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setModified(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setModifiedDate(cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6));
        entity.setContentsUrl(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setChaptersUrl(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setSlug(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setTrophiesCount(cursor.isNull(offset + 10) ? null : cursor.getInt(offset + 10));
        entity.setChaptersCount(cursor.isNull(offset + 11) ? null : cursor.getInt(offset + 11));
        entity.setContentsCount(cursor.isNull(offset + 12) ? null : cursor.getInt(offset + 12));
        entity.setOrder(cursor.isNull(offset + 13) ? null : cursor.getInt(offset + 13));
        entity.setActive(cursor.isNull(offset + 14) ? null : cursor.getShort(offset + 14) != 0);
        entity.setExternal_content_link(cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15));
        entity.setExternal_link_label(cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16));
        entity.setChildItemsLoaded(cursor.getShort(offset + 17) != 0);
        entity.setIsProduct(cursor.isNull(offset + 18) ? null : cursor.getShort(offset + 18) != 0);
        entity.setIsMyCourse(cursor.isNull(offset + 19) ? null : cursor.getShort(offset + 19) != 0);
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Course entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Course entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Course entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
