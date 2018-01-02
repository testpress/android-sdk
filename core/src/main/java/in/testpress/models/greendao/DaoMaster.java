package in.testpress.models.greendao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

import org.greenrobot.greendao.AbstractDaoMaster;
import org.greenrobot.greendao.database.StandardDatabase;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseOpenHelper;
import org.greenrobot.greendao.identityscope.IdentityScopeType;


// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/**
 * Master of DAO (schema version 8): knows all DAOs.
 */
public class DaoMaster extends AbstractDaoMaster {
    public static final int SCHEMA_VERSION = 8;

    /** Creates underlying database table using DAOs. */
    public static void createAllTables(Database db, boolean ifNotExists) {
        ReviewAttemptDao.createTable(db, ifNotExists);
        ReviewItemDao.createTable(db, ifNotExists);
        ReviewQuestionDao.createTable(db, ifNotExists);
        ReviewAnswerDao.createTable(db, ifNotExists);
        ReviewQuestionTranslationDao.createTable(db, ifNotExists);
        ReviewAnswerTranslationDao.createTable(db, ifNotExists);
        SelectedAnswerDao.createTable(db, ifNotExists);
        CourseDao.createTable(db, ifNotExists);
        ChapterDao.createTable(db, ifNotExists);
        LanguageDao.createTable(db, ifNotExists);
        VideoDao.createTable(db, ifNotExists);
        AttachmentDao.createTable(db, ifNotExists);
        ExamDao.createTable(db, ifNotExists);
        ContentDao.createTable(db, ifNotExists);
        HtmlContentDao.createTable(db, ifNotExists);
        CourseAttemptDao.createTable(db, ifNotExists);
        AttemptDao.createTable(db, ifNotExists);
        CourseContentDao.createTable(db, ifNotExists);
    }

    /** Drops underlying database table using DAOs. */
    public static void dropAllTables(Database db, boolean ifExists) {
        ReviewAttemptDao.dropTable(db, ifExists);
        ReviewItemDao.dropTable(db, ifExists);
        ReviewQuestionDao.dropTable(db, ifExists);
        ReviewAnswerDao.dropTable(db, ifExists);
        ReviewQuestionTranslationDao.dropTable(db, ifExists);
        ReviewAnswerTranslationDao.dropTable(db, ifExists);
        SelectedAnswerDao.dropTable(db, ifExists);
        CourseDao.dropTable(db, ifExists);
        ChapterDao.dropTable(db, ifExists);
        LanguageDao.dropTable(db, ifExists);
        VideoDao.dropTable(db, ifExists);
        AttachmentDao.dropTable(db, ifExists);
        ExamDao.dropTable(db, ifExists);
        ContentDao.dropTable(db, ifExists);
        HtmlContentDao.dropTable(db, ifExists);
        CourseAttemptDao.dropTable(db, ifExists);
        AttemptDao.dropTable(db, ifExists);
        CourseContentDao.dropTable(db, ifExists);
    }

    /**
     * WARNING: Drops all table on Upgrade! Use only during development.
     * Convenience method using a {@link DevOpenHelper}.
     */
    public static DaoSession newDevSession(Context context, String name) {
        Database db = new DevOpenHelper(context, name).getWritableDb();
        DaoMaster daoMaster = new DaoMaster(db);
        return daoMaster.newSession();
    }

    public DaoMaster(SQLiteDatabase db) {
        this(new StandardDatabase(db));
    }

    public DaoMaster(Database db) {
        super(db, SCHEMA_VERSION);
        registerDaoClass(ReviewAttemptDao.class);
        registerDaoClass(ReviewItemDao.class);
        registerDaoClass(ReviewQuestionDao.class);
        registerDaoClass(ReviewAnswerDao.class);
        registerDaoClass(ReviewQuestionTranslationDao.class);
        registerDaoClass(ReviewAnswerTranslationDao.class);
        registerDaoClass(SelectedAnswerDao.class);
        registerDaoClass(CourseDao.class);
        registerDaoClass(ChapterDao.class);
        registerDaoClass(LanguageDao.class);
        registerDaoClass(VideoDao.class);
        registerDaoClass(AttachmentDao.class);
        registerDaoClass(ExamDao.class);
        registerDaoClass(ContentDao.class);
        registerDaoClass(HtmlContentDao.class);
        registerDaoClass(CourseAttemptDao.class);
        registerDaoClass(AttemptDao.class);
        registerDaoClass(CourseContentDao.class);
    }

    public DaoSession newSession() {
        return new DaoSession(db, IdentityScopeType.Session, daoConfigMap);
    }

    public DaoSession newSession(IdentityScopeType type) {
        return new DaoSession(db, type, daoConfigMap);
    }

    /**
     * Calls {@link #createAllTables(Database, boolean)} in {@link #onCreate(Database)} -
     */
    public static abstract class OpenHelper extends DatabaseOpenHelper {
        public OpenHelper(Context context, String name) {
            super(context, name, SCHEMA_VERSION);
        }

        public OpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory, SCHEMA_VERSION);
        }

        @Override
        public void onCreate(Database db) {
            Log.i("greenDAO", "Creating tables for schema version " + SCHEMA_VERSION);
            createAllTables(db, false);
        }
    }

    /** WARNING: Drops all table on Upgrade! Use only during development. */
    public static class DevOpenHelper extends OpenHelper {
        public DevOpenHelper(Context context, String name) {
            super(context, name);
        }

        public DevOpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(Database db, int oldVersion, int newVersion) {
            Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
            dropAllTables(db, true);
            onCreate(db);
        }
    }

}
